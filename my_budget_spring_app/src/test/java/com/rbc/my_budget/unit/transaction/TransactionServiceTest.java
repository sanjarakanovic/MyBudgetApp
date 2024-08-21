package com.rbc.my_budget.unit.transaction;

import com.rbc.my_budget.account.Account;
import com.rbc.my_budget.account.AccountRepository;
import com.rbc.my_budget.currency.CurrencyService;
import com.rbc.my_budget.exception.NotFoundException;
import com.rbc.my_budget.transaction.Transaction;
import com.rbc.my_budget.transaction.TransactionRepository;
import com.rbc.my_budget.transaction.TransactionService;
import com.rbc.my_budget.transaction.Type;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CurrencyService currencyService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_get_all_transactions_success() {

        List<Transaction> transactions = List.of(new Transaction(), new Transaction());
        when(transactionRepository.findAll()).thenReturn(transactions);

        List<Transaction> result = transactionService.getAllTransactions();

        assertEquals(2, result.size());
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    public void test_retrieve_transaction_by_id_successfully() {

        Long transactionId = 1L;
        Transaction transaction = new Transaction();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        Transaction result = transactionService.getTransaction(transactionId);

        assertEquals(transaction, result);
        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    public void test_get_transaction_not_found() {

        Long nonExistentId = 999L;
        when(transactionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> transactionService.getTransaction(nonExistentId));
        verify(transactionRepository, times(1)).findById(nonExistentId);
    }

    @Test
    public void test_get_transactions_for_account_success() {

        String accountName = "TestAccount";
        List<Transaction> transactions = List.of(new Transaction(), new Transaction());
        when(transactionRepository.findByAccountName(accountName)).thenReturn(transactions);

        List<Transaction> result = transactionService.getTransactionsForAccount(accountName);

        assertEquals(2, result.size());
        verify(transactionRepository, times(1)).findByAccountName(accountName);
    }

    @Test
    public void test_get_transactions_for_non_existing_account_success() {

        String accountName = "noneExistingAccount";
        when(transactionRepository.findByAccountName(accountName)).thenReturn(List.of());

        List<Transaction> transactions = transactionService.getTransactionsForAccount(accountName);
        assertTrue(transactions.isEmpty());

        verify(transactionRepository, times(1)).findByAccountName(accountName);
    }


    @Test
    public void test_create_credit_transaction_update_balance() {

        Account account = new Account();
        account.setName("TestAccount");
        account.setBalance(BigDecimal.valueOf(100));
        account.setCurrency("USD");

        Transaction transaction = new Transaction();
        transaction.setType(Type.CREDIT);
        transaction.setAccount(account);
        transaction.setDescription("Test Transaction");
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setCurrency("USD");

        when(accountRepository.findById(anyString())).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            savedTransaction.setAccount(account);
            return savedTransaction;
        });
        Transaction createdTransaction = transactionService.createTransaction(transaction);

        assertNotNull(createdTransaction);
        assertEquals(BigDecimal.valueOf(200), createdTransaction.getAccount().getBalance());

        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    public void test_create_debit_transaction_update_balance() {

        Account account = new Account();
        account.setName("TestAccount");
        account.setBalance(BigDecimal.valueOf(200));
        account.setCurrency("USD");

        Transaction transaction = new Transaction();
        transaction.setType(Type.DEBIT);
        transaction.setAccount(account);
        transaction.setDescription("Test Transaction");
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setCurrency("USD");

        when(accountRepository.findById(anyString())).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            savedTransaction.setAccount(account);
            return savedTransaction;
        });
        Transaction createdTransaction = transactionService.createTransaction(transaction);

        assertNotNull(createdTransaction);
        assertEquals(BigDecimal.valueOf(100), createdTransaction.getAccount().getBalance());

        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    public void create_transaction_with_non_existent_account() {

        Transaction transaction = new Transaction();
        transaction.setAccount(new Account("non_existent_account", BigDecimal.TEN, "USD"));
    
        when(accountRepository.findById(anyString())).thenThrow(new NotFoundException(Account.class, "non_existent_account"));

        assertThrows(NotFoundException.class, () -> transactionService.createTransaction(transaction));

        verify(accountRepository, times(1)).findById("non_existent_account");
        verifyNoMoreInteractions(transactionRepository, accountRepository);
    }

    @Test
    public void test_create_transaction_with_currency_conversion() {

        Account account = new Account();
        account.setName("account");
        account.setCurrency("USD");
        account.setBalance(BigDecimal.valueOf(1000));

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setCurrency("EUR");
        transaction.setType(Type.CREDIT);

        when(accountRepository.findById("account")).thenReturn(Optional.of(account));
        when(currencyService.getExchangeRate("EUR", "USD")).thenReturn(BigDecimal.valueOf(1.2));

        transactionService.createTransaction(transaction);

        verify(accountRepository).save(argThat(acc ->
                acc.getBalance().equals(BigDecimal.valueOf(1000).add(BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(1.2)))) &&
                        acc.getCurrency().equals("USD")));
    }

    @Test
    public void test_edit_transaction() {

        Account account = new Account();
        account.setName("TestAccount");
        account.setBalance(BigDecimal.valueOf(200));
        account.setCurrency("USD");

        Transaction initialTransaction = new Transaction();
        initialTransaction.setId(1L);
        initialTransaction.setType(Type.DEBIT);
        initialTransaction.setAccount(account);
        initialTransaction.setDescription("Initial Transaction");
        initialTransaction.setAmount(BigDecimal.valueOf(50));
        initialTransaction.setCurrency("USD");

        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(1L);
        updatedTransaction.setType(Type.CREDIT);
        updatedTransaction.setAccount(account);
        updatedTransaction.setDescription("Updated Transaction");
        updatedTransaction.setAmount(BigDecimal.valueOf(75));
        updatedTransaction.setCurrency("USD");

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(initialTransaction));
        when(accountRepository.findById("TestAccount")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction resultTransaction = transactionService.editTransaction(1L, updatedTransaction);

        assertNotNull(resultTransaction);
        assertEquals(updatedTransaction.getId(), resultTransaction.getId());
        assertEquals(updatedTransaction.getType(), resultTransaction.getType());
        assertEquals(updatedTransaction.getDescription(), resultTransaction.getDescription());
        assertEquals(updatedTransaction.getAmount(), resultTransaction.getAmount());
        assertEquals(updatedTransaction.getCurrency(), resultTransaction.getCurrency());

        assertEquals(BigDecimal.valueOf(200).add(BigDecimal.valueOf(50)).add(BigDecimal.valueOf(75)), account.getBalance());

    }


    @Test
    public void edit_transaction_non_existent_id() {

        Long nonExistentId = 100L;
        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(nonExistentId);

        when(transactionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> transactionService.editTransaction(nonExistentId, updatedTransaction));

        verify(transactionRepository, times(1)).findById(nonExistentId);
    }



    @Test
    public void test_delete_transaction_valid_id() {

        Account account = new Account();
        account.setName("TestAccount");
        account.setBalance(BigDecimal.valueOf(200));
        account.setCurrency("USD");

        Transaction transaction = new Transaction();
        transaction.setType(Type.DEBIT);
        transaction.setAccount(account);
        transaction.setDescription("Test Transaction");
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setCurrency("USD");

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(accountRepository.findById("TestAccount")).thenReturn(Optional.of(account));

        transactionService.deleteTransaction(1L);

        assertEquals(BigDecimal.valueOf(300), account.getBalance());
        verify(transactionRepository).delete(transaction);
    }

    @Test
    public void delete_transaction_non_existent_id() {

        Long nonExistentId = 999L;
        when(transactionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> transactionService.deleteTransaction(nonExistentId));

        verify(transactionRepository, times(1)).findById(nonExistentId);
    }

    @Test
    public void test_delete_all_transactions() {

        Account account1 = new Account();
        account1.setName("TestAccount1");
        account1.setBalance(BigDecimal.valueOf(200));
        account1.setCurrency("USD");

        Account account2 = new Account();
        account2.setName("TestAccount2");
        account2.setBalance(BigDecimal.valueOf(300));
        account2.setCurrency("USD");

        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setType(Type.DEBIT);
        transaction1.setAccount(account1);
        transaction1.setDescription("Test Transaction 1");
        transaction1.setAmount(BigDecimal.valueOf(50));
        transaction1.setCurrency("USD");

        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setType(Type.CREDIT);
        transaction2.setAccount(account2);
        transaction2.setDescription("Test Transaction 2");
        transaction2.setAmount(BigDecimal.valueOf(100));
        transaction2.setCurrency("USD");

        Transaction transaction3 = new Transaction();
        transaction3.setId(3L);
        transaction3.setType(Type.DEBIT);
        transaction3.setAccount(account2);
        transaction3.setDescription("Test Transaction 2");
        transaction3.setAmount(BigDecimal.valueOf(100));
        transaction3.setCurrency("USD");

        List<Transaction> transactions = List.of(transaction1, transaction2, transaction3);

        when(transactionRepository.findAll()).thenReturn(transactions);
        when(accountRepository.findById(account1.getName())).thenReturn(Optional.of(account1));
        when(accountRepository.findById(account2.getName())).thenReturn(Optional.of(account2));

        transactionService.deleteAllTransactions();

        verify(transactionRepository).deleteAll();

        assertEquals(BigDecimal.valueOf(250), account1.getBalance());
        assertEquals(BigDecimal.valueOf(300), account2.getBalance());
    }


}