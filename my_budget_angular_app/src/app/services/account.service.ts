import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Account } from '../models/account';
import { Observable } from 'rxjs/internal/Observable';
import { BehaviorSubject, tap, switchMap, map } from 'rxjs';
import { CurrencyService } from './currency.service';


@Injectable({
  providedIn: 'root'
})
export class AccountService {

  private baseAccountsUrl = 'http://localhost:8080/accounts'
  private singleAccountUrl = 'http://localhost:8080/accounts/account'

  private accountsSubject = new BehaviorSubject<Account[]>([]);
  accounts$ = this.accountsSubject.asObservable();

  private totalAmountSubject = new BehaviorSubject<number>(0);
  totalAmount$ = this.totalAmountSubject.asObservable();


  constructor(private httpClient: HttpClient, private currencyService: CurrencyService) {
    this.loadAccounts(); 
  }


  loadAccounts(): void {
    this.currencyService.currencyRatesForDefaultCurrency$.pipe(
      switchMap(currencyRates => {

        return this.httpClient.get<Account[]>(this.baseAccountsUrl).pipe(

          map((accounts: Account[]) => {
            return accounts.map(account => {
              const rate = currencyRates[account.currency.toLocaleLowerCase()];
              account.balanceInDefaultCurrency = !isNaN(rate) ? account.balance / rate : account.balance;
              return account;
            });
          }),

          tap(accounts => {
            this.accountsSubject.next(accounts);
            const totalAmount = accounts.reduce((sum, account) => sum + account.balanceInDefaultCurrency, 0);
            this.totalAmountSubject.next(totalAmount);
          })

        );
      })
    ).subscribe();
  }


  getAccount(name: string): Observable<Account> {
    return this.httpClient.get<Account>(`${this.singleAccountUrl}/${name}`);
  }

  createAccount(account: Account): Observable<Account> {
    return this.httpClient.post<Account>(this.baseAccountsUrl, account)
      .pipe(
        tap(() => this.loadAccounts()) 
      );
  }

  editAccount(name: string, account: Account): Observable<Account> {
    return this.httpClient.put<Account>(`${this.singleAccountUrl}/${name}`, account)
      .pipe(
        tap(() => this.loadAccounts()) 
      );
  }

  deleteAccount(name: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.singleAccountUrl}/${name}`)
      .pipe(
        tap(() => this.loadAccounts()) 
      );
  }

  deleteAllAccounts(): Observable<void> {
    return this.httpClient.delete<void>(this.baseAccountsUrl)
      .pipe(
        tap(() => this.loadAccounts())
      );
  }


}
