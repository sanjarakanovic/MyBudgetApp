package com.rbc.my_budget.transaction;

import com.rbc.my_budget.account.Account;
import com.rbc.my_budget.account.AccountRepository;
import com.rbc.my_budget.currency.CurrencyService;
import com.rbc.my_budget.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CurrencyService currencyService;

    public List<Transaction> getAllTransactions(){
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsForAccount(String accountName) {
        return transactionRepository.findByAccountName(accountName);
    }

    public Transaction getTransaction(Long id){
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Transaction.class, id));
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction){
        Account account = updateAccountBalance(transaction, transaction.getType());
        transaction.setAccount(account);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction editTransaction(Long id, Transaction updatedTransaction){

        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Transaction.class, id));

        updateAccountBalance(existingTransaction,existingTransaction.getType().getOpposite());
        existingTransaction.setAccount(updateAccountBalance(updatedTransaction,updatedTransaction.getType()));

        existingTransaction.setType(updatedTransaction.getType());
        existingTransaction.setAmount(updatedTransaction.getAmount());
        existingTransaction.setCurrency(updatedTransaction.getCurrency());
        existingTransaction.setDescription(updatedTransaction.getDescription());

        return transactionRepository.save(existingTransaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {

        transactionRepository.findById(id).ifPresentOrElse(transaction -> {
                    // Inverse logic when deleting: adjust the account balance
                    updateAccountBalance(transaction, transaction.getType().getOpposite());

                    transactionRepository.delete(transaction);
                },
                () -> { throw new NotFoundException(Transaction.class, id); });
    }

    @Transactional
    public void deleteAllTransactions() {

        List<Transaction> allTransactions = transactionRepository.findAll();

        for (Transaction transaction : allTransactions)
            updateAccountBalance(transaction, transaction.getType().getOpposite());

        transactionRepository.deleteAll();
    }

    private Account updateAccountBalance(Transaction transaction, Type type){

        Account account = accountRepository.findById(transaction.getAccount().getName())
                .orElseThrow(() -> new NotFoundException(Account.class, transaction.getAccount().getName()));

        BigDecimal amount = transaction.getAmount();

        if(!account.getCurrency().equals(transaction.getCurrency()))
            amount = amount.multiply(currencyService.getExchangeRate(transaction.getCurrency(),account.getCurrency()));

        if (Type.CREDIT.equals(type))
            account.setBalance(account.getBalance().add(amount));
        else
            account.setBalance(account.getBalance().subtract(amount));

        return accountRepository.save(account);
    }
}
