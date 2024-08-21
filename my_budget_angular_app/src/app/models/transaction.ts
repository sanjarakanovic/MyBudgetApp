import { Account } from "./account";
import { TransactionType } from "./transaction-type.enum";

export interface Transaction {

  id: number;
  description: string;
  account: Account;
  amount: number;
  currency: string;
  type: TransactionType;
  amountInDefaultCurrency: number;

}
