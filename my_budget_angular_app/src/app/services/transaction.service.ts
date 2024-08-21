import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { Transaction } from '../models/transaction';
import { BehaviorSubject, tap, switchMap, map } from 'rxjs';
import { CurrencyService } from './currency.service';
import { AccountService } from './account.service';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  private baseTransactionsUrl = 'http://localhost:8080/transactions';
  private singleTransactionUrl = 'http://localhost:8080/transactions/transaction';

  private transactionsSubject = new BehaviorSubject<Transaction[]>([]);
  transactions$ = this.transactionsSubject.asObservable();


  constructor(private httpClient: HttpClient, private currencyService: CurrencyService, private accountService: AccountService) {
    this.loadTransactions(); 
  }

 
  loadTransactions(): void{
    this.currencyService.currencyRatesForDefaultCurrency$.pipe(
      switchMap(currencyRates => {

        return this.httpClient.get<Transaction[]>(this.baseTransactionsUrl).pipe(
          map((transactions: Transaction[]) => {

            return transactions.map(transaction => {

              const rate = currencyRates[transaction.currency.toLowerCase()];
              transaction.amountInDefaultCurrency = !isNaN(rate) ? transaction.amount / rate : transaction.amount;
              return transaction;
            });
          }),
          tap(transactions => this.transactionsSubject.next(transactions))
        );
      })
    ).subscribe();
  }

  getTransactionsForAccount(name: string): Observable<Transaction[]> {
    return this.httpClient.get<Transaction[]>(`${this.baseTransactionsUrl}/${name}`);
  }

  getTransaction(id: number): Observable<Transaction> {
    return this.httpClient.get<Transaction>(`${this.singleTransactionUrl}/${id}`);
  }

  createTransaction(transaction: Transaction): Observable<Transaction> {
    return this.httpClient.post<Transaction>(this.baseTransactionsUrl, transaction)
      .pipe(
        tap(() => this.loadTransactions()),
        tap(() => this.accountService.loadAccounts())
      );
  }

  editTransaction(id: number, transaction: Transaction): Observable<Transaction> {
    return this.httpClient.put<Transaction>(`${this.singleTransactionUrl}/${id}`, transaction)
      .pipe(
        tap(() => this.loadTransactions()),
        tap(() => this.accountService.loadAccounts())
      );
  }

  deleteTransaction(id: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.singleTransactionUrl}/${id}`)
      .pipe(
        tap(() => this.loadTransactions()),
        tap(() => this.accountService.loadAccounts()) 
      );
  }

  deleteAllTransactions(): Observable<void> {
    return this.httpClient.delete<void>(this.baseTransactionsUrl)
      .pipe(
        tap(() => this.loadTransactions()),
        tap(() => this.accountService.loadAccounts())
      );
  }
}
