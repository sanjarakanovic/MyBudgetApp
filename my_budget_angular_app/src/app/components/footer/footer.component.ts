import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { TransactionService } from '../../services/transaction.service';
import { TransactionDialogComponent } from '../transaction-dialog/transaction-dialog.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CurrencyService } from '../../services/currency.service';
import { AccountService } from '../../services/account.service';
import { CommonModule } from '@angular/common';
import { Transaction } from '../../models/transaction';
import { catchError, tap } from 'rxjs/operators';
import { of } from 'rxjs';



@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss'
})
export class FooterComponent {

  availableAmount :number = 0;
  defaultCurrency = "";

  

  constructor(public dialog: MatDialog, public snackBar: MatSnackBar, private transactionService: TransactionService, private currencyService: CurrencyService,
  private accountService:AccountService) { }


  ngOnInit() {
    this.currencyService.defaultCurrency$.subscribe(currency => {
      this.defaultCurrency = currency;
    });
    this.accountService.totalAmount$.subscribe(amount => {
      this.availableAmount = amount;
    });
  }

  openCreateTransactionDialog(): void {
    const dialogRef = this.dialog.open(TransactionDialogComponent, {
      disableClose: true,
    });

    dialogRef.afterClosed().pipe(
      
      tap((result: Transaction) => {
        if (result) {
          this.transactionService.createTransaction(result).pipe(
            tap(() => {
              this.snackBar.open(`Success: Transaction created.`, 'Close', { duration: 5000 });
            }),
            catchError(err => {
              const errorMessage = typeof err.error === 'object' ? 'Something went wrong' : `Error: ${err.error}`
              this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
              return of(null); 
            })
          ).subscribe(); 
        }
      }),
      catchError(error => {
        console.error('Dialog close error:', error); 
        return of(null);
      })
    ).subscribe();
  }

}
