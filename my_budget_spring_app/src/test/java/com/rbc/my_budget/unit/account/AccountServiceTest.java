package com.rbc.my_budget.unit.account;

import com.rbc.my_budget.account.Account;
import com.rbc.my_budget.account.AccountRepository;
import com.rbc.my_budget.account.AccountService;
import com.rbc.my_budget.exception.AccountAlreadyExistsException;
import com.rbc.my_budget.exception.AccountCannotBeDeletedException;
import com.rbc.my_budget.exception.NotFoundException;
import com.rbc.my_budget.transaction.Transaction;
import com.rbc.my_budget.transaction.TransactionRepository;
import com.rbc.my_budget.transaction.Type;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

public class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_retrieve_all_accounts_successfully() {

        List<Account> accounts = List.of(
            new Account("Account1", new BigDecimal("100.00"), "USD"),
            new Account("Account2", new BigDecimal("200.00"), "EUR")
        );
    
        when(accountRepository.findAll()).thenReturn(accounts);
    
        List<Account> result = accountService.getAllAccounts();
    
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Account1", result.get(0).getName());
        Assertions.assertEquals("Account2", result.get(1).getName());
    }

    @Test
    public void test_retrieve_specific_account_successfully() {

        Account testAccount = new Account("TestAccount", new BigDecimal("500.00"), "CAD");

        when(accountRepository.findById("TestAccount")).thenReturn(Optional.of(testAccount));

        Account result = accountService.getAccount("TestAccount");

        Assertions.assertEquals("TestAccount", result.getName());
        Assertions.assertEquals(new BigDecimal("500.00"), result.getBalance());
        Assertions.assertEquals("CAD", result.getCurrency());
    }

    @Test
    public void test_retrieve_non_existent_account() {
        String nonExistentAccountName = "NonExistentAccount";

        when(accountRepository.findById(nonExistentAccountName)).thenReturn(Optional.empty());

        NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> accountService.getAccount(nonExistentAccountName)
        );

        Assertions.assertEquals("Account not found with ID: NonExistentAccount", exception.getMessage());
    }

    @Test
    public void test_create_account_successfully() {

        Account newAccount = new Account("TestAccount", new BigDecimal("500.00"), "CAD");

        when(accountRepository.existsById(newAccount.getName())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(newAccount);

        Account createdAccount = accountService.createAccount(newAccount);

        verify(accountRepository, times(1)).save(newAccount);

        verify(accountRepository, times(1)).existsById(newAccount.getName());

        Assertions.assertNotNull(createdAccount);
        Assertions.assertEquals("TestAccount", createdAccount.getName());
        Assertions.assertEquals(new BigDecimal("500.00"), createdAccount.getBalance());
        Assertions.assertEquals("CAD", createdAccount.getCurrency());
    }

    @Test
    public void test_attempt_create_existing_account() {

        AccountRepository accountRepository = Mockito.mock(AccountRepository.class);
        AccountService accountService = new AccountService(accountRepository);

        Account existingAccount = new Account("ExistingAccount", new BigDecimal("500.00"), "CAD");

        when(accountRepository.existsById(existingAccount.getName())).thenReturn(true);

        Assertions.assertThrows(AccountAlreadyExistsException.class, () -> accountService.createAccount(existingAccount));
    }


    @Test
    public void edit_existing_account_successfully() {

        String accountName = "TestAccount";
        Account existingAccount = new Account(accountName, new BigDecimal("500.00"), "USD");
        Account updatedAccount = new Account(accountName, new BigDecimal("700.00"), "EUR");

        when(accountRepository.findById(accountName)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(Mockito.any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountService.editAccount(accountName, updatedAccount);

        Assertions.assertEquals(accountName, result.getName());
        Assertions.assertEquals(new BigDecimal("700.00"), result.getBalance());
        Assertions.assertEquals("EUR", result.getCurrency());
    }

    @Test
    public void test_edit_non_existent_account() {
        AccountRepository accountRepository = Mockito.mock(AccountRepository.class);
        AccountService accountService = new AccountService(accountRepository);

        String nonExistentAccountName = "NonExistentAccount";
        Account updatedAccount = new Account("NonExistentAccount", new BigDecimal("500.00"), "EUR");

        when(accountRepository.findById(nonExistentAccountName)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> accountService.editAccount(nonExistentAccountName, updatedAccount));
    }

    @Test
    public void delete_existing_account_successfully() {

        String accountName = "TestAccount";
        Account account = new Account(accountName, new BigDecimal("500.00"), "USD");

        when(accountRepository.findById(accountName)).thenReturn(Optional.of(account));

        accountService.deleteAccount(accountName);

        Mockito.verify(accountRepository, times(1)).delete(account);
    }

    @Test
    public void test_delete_non_existent_account() {
        AccountRepository accountRepository = Mockito.mock(AccountRepository.class);
        AccountService accountService = new AccountService(accountRepository);

        String nonExistentAccountName = "NonExistentAccount";

        when(accountRepository.findById(nonExistentAccountName)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> accountService.deleteAccount(nonExistentAccountName));

        assertEquals("Account not found with ID: NonExistentAccount", exception.getMessage());
    }

    @Test
    public void test_delete_account_with_transactions() {

        String accountName = "TestAccount";
        Account account = new Account(accountName, new BigDecimal("100.00"), "USD");
        Transaction transaction = new Transaction(1L, Type.CREDIT, account, "Test desc", new BigDecimal("50.00"), "USD");
        TransactionRepository transactionRepository = mock(TransactionRepository.class);

        when(accountRepository.findById(accountName)).thenReturn(Optional.of(account));
        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));

        doThrow(new DataIntegrityViolationException("Test exception")).when(accountRepository).delete(account);

        Assertions.assertThrows(AccountCannotBeDeletedException.class, () -> accountService.deleteAccount(accountName));
    }


    @Test
    public void delete_all_accounts_successfully() {

        Mockito.doNothing().when(accountRepository).deleteAll();
        Assertions.assertDoesNotThrow(() -> accountService.deleteAllAccounts());
    }


    @Test
    public void test_delete_all_accounts_with_associated_transactions() {

        Mockito.doThrow(DataIntegrityViolationException.class).when(accountRepository).deleteAll();

        Assertions.assertThrows(AccountCannotBeDeletedException.class, () -> accountService.deleteAllAccounts());
    }

    @Test
    public void test_delete_all_accounts_transactional_integrity() {

        Mockito.doThrow(DataIntegrityViolationException.class).when(accountRepository).deleteAll();

        Assertions.assertThrows(AccountCannotBeDeletedException.class, accountService::deleteAllAccounts);
    }

    @Test
    public void test_validate_account_creation_with_null_or_invalid_fields() {

        Account existingAccount = new Account("ExistingAccount", new BigDecimal("500.00"), "USD");
        when(accountRepository.existsById(existingAccount.getName())).thenReturn(true);

        Account newAccount = new Account("", null, null);
        Account finalNewAccount1 = newAccount;
        Assertions.assertDoesNotThrow(() -> accountService.createAccount(finalNewAccount1));

        newAccount = new Account("NewAccount", null, "EUR");
        Account finalNewAccount2 = newAccount;
        Assertions.assertDoesNotThrow(() -> accountService.createAccount(finalNewAccount2));

        newAccount = new Account("AnotherNewAccount", new BigDecimal("1000.00"), null);
        Account finalNewAccount3 = newAccount;
        Assertions.assertDoesNotThrow(() -> accountService.createAccount(finalNewAccount3));
    }

}