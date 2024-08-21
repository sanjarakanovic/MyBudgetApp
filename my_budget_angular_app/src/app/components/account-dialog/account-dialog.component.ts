import { Component, Inject, OnInit } from '@angular/core';
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
import { map, startWith} from 'rxjs/operators';

@Component({
  selector: 'app-account-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCardModule,
    MatAutocompleteModule,
    ReactiveFormsModule
  ],
  templateUrl: './account-dialog.component.html',
  styleUrls: ['./account-dialog.component.scss']
})
export class AccountDialogComponent implements OnInit {

  accountForm: FormGroup;
  currencies: string[] = [];
  filteredCurrencies: Observable<string[]> = of([]);
  isEditing: boolean = false;


  constructor(
    private fb: FormBuilder,
    private currencyService: CurrencyService,
    public dialogRef: MatDialogRef<AccountDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    if (data && data.account) 
      this.isEditing = true;

    this.accountForm = this.fb.group({
      name: [{ value: this.data?.account?.name || '', disabled: this.isEditing }, Validators.required],
      balance: [this.data?.account?.balance || null, [Validators.required]],
      currency: [this.data?.account?.currency || '', Validators.required]
    });
  }


  ngOnInit() {
    this.currencyService.currencies$.subscribe(currencies => {
      this.currencies = currencies;
      this.filteredCurrencies = this.accountForm.get('currency')!.valueChanges.pipe(
        startWith(''),
        map(value => this.filterCurrencies(value))
      );
    });
  }


  private filterCurrencies(value: string): string[] {
    return this.currencies.filter(currency => currency.includes(value));
  }


  onCancel(): void {
    this.dialogRef.close();
  }


  onSave(): void {
    if (this.accountForm.valid) {
      // Include the 'name' field even if it's disabled
      const formValue = { ...this.accountForm.getRawValue() };
      this.dialogRef.close(formValue);
    }
  }

}
