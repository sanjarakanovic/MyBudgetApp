import { Component } from '@angular/core';
import { Account } from '../../models/account';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { AccountDialogComponent } from '../account-dialog/account-dialog.component';
import { AccountService } from '../../services/account.service';
import { CurrencyService } from '../../services/currency.service';
import { CommonModule } from '@angular/common';
import { Observable, catchError, of, tap } from 'rxjs';


@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatToolbarModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatOptionModule,
    MatTableModule],
  templateUrl: './accounts.component.html',
  styleUrl: './accounts.component.scss'
})
export class AccountsComponent {

  displayedColumns: string[] = ['name', 'balance', 'actions'];
  defaultCurrency = "";
  accounts = new MatTableDataSource<Account>([]);


  constructor(public dialog: MatDialog, public snackBar: MatSnackBar, private accountService: AccountService, private currencyService: CurrencyService) { }


  ngOnInit(): void {

    this.currencyService.defaultCurrency$.subscribe(currency => 
      this.defaultCurrency = currency
    );

    this.accountService.accounts$.subscribe(accounts =>
      this.accounts.data = accounts
    );
  }


  private handleAccountAction(
    action: 'create' | 'edit' | 'delete',
    account?: Account
  ): void {
    let dialogRef;
    let actionObservable;
    let successMessage: string;

    if (action !== 'delete') {
      dialogRef = this.dialog.open(AccountDialogComponent, {
        data: { account },
        disableClose: action === 'create',
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          switch (action) {
            case 'create':
              actionObservable = this.accountService.createAccount(result);
              successMessage = `Success: Account ${result.name} created.`;
              break;
            case 'edit':
              actionObservable = this.accountService.editAccount(result.name, result);
              successMessage = `Success: Account updated.`;
              break;
          }
          this.handleActionObservable(actionObservable, successMessage);
        }
      });
    } else {
      const confirmed = window.confirm('Are you sure you want to delete this account? This action cannot be undone.');
      if (confirmed && account) {
        actionObservable = this.accountService.deleteAccount(account.name);
        successMessage = `Success: Account deleted.`;
        this.handleActionObservable(actionObservable, successMessage);
      }
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

  openCreateAccountDialog(): void {
    this.handleAccountAction('create');
  }

  openEditDialog(account: Account): void {
    this.handleAccountAction('edit', account);
  }

  deleteAccount(account: Account): void {
    this.handleAccountAction('delete', account);
  }



}
