import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { CurrencyService } from '../../services/currency.service';
import { Observable, of } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { TransactionType } from '../../models/transaction-type.enum';
import { AccountService } from '../../services/account.service';
import { Account } from '../../models/account';

@Component({
  selector: 'app-transaction-dialog',
  standalone: true,
  imports: [CommonModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCardModule,
    MatAutocompleteModule,
    ReactiveFormsModule],
  templateUrl: './transaction-dialog.component.html',
  styleUrl: './transaction-dialog.component.scss'
})
export class TransactionDialogComponent {


  transactionForm: FormGroup;
  types = Object.values(TransactionType);
  accounts: Account[] = [];
  currencies: string[] = [];
  filteredCurrencies: Observable<string[]> = of([]);
  isEditing: boolean = false;


  constructor(
    private fb: FormBuilder,
    private currencyService: CurrencyService,
    private accountService: AccountService,
    public dialogRef: MatDialogRef<TransactionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {

    if (data && data.transaction) 
      this.isEditing = true;

      this.transactionForm = this.fb.group({
      description: [this.data?.transaction?.description || '', Validators.required],
      amount: [this.data?.transaction?.amount || null, [Validators.required, Validators.min(0.01)]],
      type: [this.data?.transaction?.type || '', Validators.required],
      account: [this.data?.transaction?.account || '', Validators.required],
      currency: [this.data?.transaction?.currency || '', Validators.required]
    });
  }


  ngOnInit() {
    this.currencyService.currencies$.subscribe(currencies => {
      this.currencies = currencies;
      this.filteredCurrencies = this.transactionForm.get('currency')!.valueChanges.pipe(
        startWith(''),
        map(value => this.filterCurrencies(value))
      );
    });

    this.accountService.accounts$.subscribe(accounts => {
      this.accounts = accounts;
    });
  }


  private filterCurrencies(value: string): string[] {
    return this.currencies.filter(currency => currency.includes(value));
  }


  onCancel(): void {
    this.dialogRef.close();
  }


  onSave(): void {

    if (this.transactionForm.valid) {
      if (!this.isEditing)
        this.dialogRef.close(this.transactionForm.value);
      else{
        const updatedTransaction = this.transactionForm.value;
        const id = this.data.transaction?.id;
        this.dialogRef.close({ id, ...updatedTransaction });
      }
    }
  }

}
