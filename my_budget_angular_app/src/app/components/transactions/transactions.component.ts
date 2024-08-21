import { Component } from '@angular/core';
import { Transaction } from '../../models/transaction';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';  
import { TransactionService } from '../../services/transaction.service';
import { Account } from '../../models/account';
import { AccountService } from '../../services/account.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { CurrencyService } from '../../services/currency.service';
import { TransactionType } from '../../models/transaction-type.enum';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { TransactionDialogComponent } from '../transaction-dialog/transaction-dialog.component';
import { catchError, tap } from 'rxjs/operators';
import { Observable, of } from 'rxjs';
@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatToolbarModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatOptionModule,
    MatTableModule],
  templateUrl: './transactions.component.html',
  styleUrl: './transactions.component.scss'
})
export class TransactionsComponent {

  displayedColumns: string[] = ['description', 'amount', 'actions'];
  transactions = new MatTableDataSource<Transaction>([]);
  filteredTransactions = this.transactions;
  accounts: Account[] = []
  selectedAccount: string = '';
  defaultCurrency:string = "";
  public TransactionType = TransactionType; 


  constructor(private transactionService: TransactionService, private accountService: AccountService, private currencyService: CurrencyService,
    private dialog: MatDialog, public snackBar: MatSnackBar) { }

  ngOnInit(): void {
    this.currencyService.defaultCurrency$.subscribe(currency => {
      this.defaultCurrency = currency;
    });

    this.transactionService.transactions$.subscribe(
      transactions => this.transactions.data = transactions,
    );
    this.accountService.accounts$.subscribe(accounts => {
      this.accounts = accounts;
    });

    this.transactions.filterPredicate = (transaction: Transaction, filter: string) => {
      return transaction.account.name.trim().toLowerCase() === filter.trim().toLowerCase() || filter === '';
    };
  }


  filterTransactions() {
    this.transactions.filter = this.selectedAccount ? this.selectedAccount : '';
  }


  openEditDialog(transaction: any): void {
    const dialogRef = this.dialog.open(TransactionDialogComponent, {
      data: { transaction },
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const { id, ...updatedTransaction } = result;
        const editObservable = this.transactionService.editTransaction(id, updatedTransaction);
        this.handleActionObservable(editObservable, 'Success: Transaction updated.');
      }
    });
  }


  deleteTransaction(transaction: Transaction) {
    const confirmed = window.confirm('Are you sure you want to delete transaction? This action cannot be undone.');

    if (confirmed) {
      const deleteObservable = this.transactionService.deleteTransaction(transaction.id);
      this.handleActionObservable(deleteObservable, 'Success: Transaction deleted.');
    }
  }


  private handleActionObservable(actionObservable: Observable<any>, successMessage: string): void {
    actionObservable.pipe(
      tap(() => {
        this.snackBar.open(successMessage, 'Close', { duration: 5000 });
      }),
      catchError(err => {
        const errorMessage = typeof err.error === 'object' ? 'Something went wrong' : `Error: ${err.error}`
        this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
        return of(null);
      })
    ).subscribe();
  }

}
