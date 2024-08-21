import { HttpClient } from '@angular/common/http';
import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { BehaviorSubject, of } from 'rxjs';
import { catchError, tap, switchMap } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';



@Injectable({
  providedIn: 'root'
})
export class CurrencyService {

  private currenciesListUrl = 'https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies.json';
  private exchangeApiUrl = 'https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/';

  private currenciesSubject = new BehaviorSubject<string[]>([]);
  public currencies$ = this.currenciesSubject.asObservable();

  private defaultCurrencyKey = 'defaultCurrency';
  private defaultCurrencySubject = new BehaviorSubject<string>(this.loadDefaultCurrency());
  public defaultCurrency$ = this.defaultCurrencySubject.asObservable();

  private currencyRatesForDefaultCurrencySubject = new BehaviorSubject<Record<string, number>>({});
  public currencyRatesForDefaultCurrency$ = this.currencyRatesForDefaultCurrencySubject.asObservable();

  private dateOfRateUpdatedSubject = new BehaviorSubject<string>('');
  public dateOfRateUpdated$ = this.dateOfRateUpdatedSubject.asObservable();


  constructor(private http: HttpClient, @Inject(PLATFORM_ID) private platformId: Object) {

    this.loadCurrencies();
    this.loadCurrencyRatesForDefaultCurrency();

  }

  private loadCurrencies(): void {

    this.http.get<string[]>(this.currenciesListUrl).pipe(

      tap(response => {
        const currencyCodes = Object.keys(response).map(code => code.toUpperCase());
        this.currenciesSubject.next(currencyCodes);
      }),

      catchError(error => {
        console.error('Failed to load currencies:', error.message);
        return of([]);
      })

    ).subscribe();
  }


  private loadDefaultCurrency(): string {

    if (isPlatformBrowser(this.platformId))
      return localStorage.getItem(this.defaultCurrencyKey) || 'EUR';

    return 'EUR';
  }


  setDefaultCurrency(currency: string): void {

    if (isPlatformBrowser(this.platformId)) {

      localStorage.setItem(this.defaultCurrencyKey, currency);
      this.defaultCurrencySubject.next(currency);
      this.loadCurrencyRatesForDefaultCurrency();
    }
  }



  private loadCurrencyRatesForDefaultCurrency1() {

    this.defaultCurrency$.pipe(

      switchMap(defaultCurrency =>
        this.http.get<any>(`${this.exchangeApiUrl}${defaultCurrency.toLowerCase()}.json`).pipe(

          tap(response => {
            this.dateOfRateUpdatedSubject.next(response.date);
            this.currencyRatesForDefaultCurrencySubject.next(response[defaultCurrency.toLowerCase()]);
          }),

          catchError(error => {
            console.error('Error fetching currency rates:', error.message);
            return of({ date: '', [defaultCurrency]: {} });
          })
        )
      )
    ).subscribe();
  }

  private loadCurrencyRatesForDefaultCurrency() {
    const defaultCurrency = this.defaultCurrencySubject.getValue();

    this.http.get<any>(`${this.exchangeApiUrl}${defaultCurrency.toLowerCase()}.json`).subscribe(
      response => {
        this.dateOfRateUpdatedSubject.next(response.date);
        this.currencyRatesForDefaultCurrencySubject.next(response[defaultCurrency.toLowerCase()]);
      },
      error => {
        console.error('Error fetching currency rates:', error);
      }
    );
  }

}

