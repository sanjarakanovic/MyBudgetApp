import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, forkJoin, of } from 'rxjs';
import { catchError, concatMap, map, startWith, tap } from 'rxjs/operators';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { CurrencyService } from '../../services/currency.service';
import { MatOptionModule } from '@angular/material/core';
import { CommonModule } from '@angular/common';
import { TransactionService } from '../../services/transaction.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AccountService } from '../../services/account.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatOptionModule,
    MatButtonModule,
    MatAutocompleteModule
  ],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {

  currencyForm: FormGroup;
  currencies: string[] = [];
  filteredCurrencies: Observable<string[]> = of([]);
  dateOfRateUpdated: string = "";
  defaultCurrency: string = "";


  constructor(
    private fb: FormBuilder,
    private currencyService: CurrencyService,
    private transactionService: TransactionService,
    private accountService: AccountService,
    public snackBar: MatSnackBar
  ) {
    this.currencyForm = this.fb.group({
      currency: []
    });

  }

  ngOnInit() {

    this.currencyService.defaultCurrency$.subscribe(currency => {
      this.defaultCurrency = currency;
    });

    this.currencyForm.patchValue({
      currency: this.defaultCurrency
    });

    this.currencyService.dateOfRateUpdated$.subscribe(date => {
      this.dateOfRateUpdated = date;
    });

    this.currencyService.currencies$.subscribe(currencies => {
      this.currencies = currencies;
      this.filteredCurrencies = this.currencyForm.get('currency')!.valueChanges.pipe(
        startWith(''),
        map(value => this.filterCurrencies(value))
      );
    });
    this.currencyForm.get('currency')?.valueChanges.subscribe(selectedCurrency => {
      if (selectedCurrency && this.currencies.includes(selectedCurrency)) {
        this.currencyService.setDefaultCurrency(selectedCurrency);
      }
    });

  }

  private filterCurrencies(value: string): string[] {
    const result = this.currencies.filter(currency => currency.includes(value.toUpperCase()));
    return result;
  }


  deleteAllData(): void {
    const confirmed = window.confirm('Are you sure you want to delete all data? This action cannot be undone.');

    if (confirmed) {
      this.transactionService.deleteAllTransactions().pipe(
        concatMap(() => this.accountService.deleteAllAccounts()), 
        tap(() => {
          this.snackBar.open('Success: All data deleted.', 'Close', {
            duration: 5000
          });
        }),
        catchError(err => {
          const errorMessage = typeof err.error === 'object' ? 'Something went wrong' : `Error: ${err.error}`;
          this.snackBar.open(errorMessage, 'Close', {
            duration: 5000
          });
          return of(null); 
        })
      ).subscribe();
    }
  }
}
