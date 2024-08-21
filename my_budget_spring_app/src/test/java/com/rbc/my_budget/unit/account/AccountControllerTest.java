package com.rbc.my_budget.unit.account;

import com.rbc.my_budget.account.Account;
import com.rbc.my_budget.account.AccountController;
import com.rbc.my_budget.account.AccountService;
import com.rbc.my_budget.exception.AccountAlreadyExistsException;
import com.rbc.my_budget.exception.AccountCannotBeDeletedException;
import com.rbc.my_budget.exception.NotFoundException;
import com.rbc.my_budget.transaction.Transaction;
import com.rbc.my_budget.transaction.TransactionService;
import com.rbc.my_budget.transaction.Type;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

public class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @Mock
    private TransactionService transactionService;


    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_retrieve_all_accounts_successfully() {
        List<Account> accounts = List.of(
                new Account("Account1", new BigDecimal("100.00"), "USD", null),
                new Account("Account2", new BigDecimal("200.00"), "USD", null)
        );
        when(accountService.getAllAccounts()).thenReturn(accounts);

        ResponseEntity<List<Account>> response = accountController.getAllAccounts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(accountService, times(1)).getAllAccounts();
    }

    @Test
    public void test_retrieve_specific_account_by_name_success() {
        String accountName = "TestAccount";
        Account testAccount = new Account(accountName, new BigDecimal("500.00"), "EUR");
        when(accountService.getAccount(accountName)).thenReturn(testAccount);

        ResponseEntity<Account> response = accountController.getAccount(accountName);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testAccount, response.getBody());
    }

    @Test
    public void test_retrieve_non_existent_account_by_name() {
        String nonExistentAccountName = "NonExistentAccount";
        when(accountService.getAccount(nonExistentAccountName))
                .thenThrow(new NotFoundException(Account.class, nonExistentAccountName));

        assertThrows(NotFoundException.class, () -> accountController.getAccount(nonExistentAccountName));
        verify(accountService, times(1)).getAccount(nonExistentAccountName);
    }

    @Test
    public void test_create_new_account_with_valid_data() {

        Account newAccount = new Account("NewAccount", new BigDecimal("500.00"), "EUR");
        when(accountService.createAccount(newAccount)).thenReturn(newAccount);

        ResponseEntity<Account> response = accountController.createAccount(newAccount);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newAccount, response.getBody());
    }

    @Test
    public void test_create_account_name_already_exists() {
        Account existingAccount = new Account("ExistingAccount", new BigDecimal("500.00"), "GBP");
        when(accountService.createAccount(existingAccount)).thenThrow(new AccountAlreadyExistsException(existingAccount.getName()));

        assertThrows(AccountAlreadyExistsException.class, () -> accountController.createAccount(existingAccount));
        verify(accountService, times(1)).createAccount(existingAccount);
    }


    @Test
    public void update_existing_account_with_valid_data() {
        String accountName = "TestAccount";
        Account existingAccount = new Account(accountName, new BigDecimal("500.00"), "USD");
        Account updatedAccount = new Account(accountName, new BigDecimal("700.00"), "EUR");

        when(accountService.getAccount(accountName)).thenReturn(existingAccount);
        when(accountService.editAccount(eq(accountName), any(Account.class))).thenReturn(updatedAccount);

        ResponseEntity<Account> response = accountController.updateAccount(accountName, updatedAccount);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedAccount, response.getBody());
    }

    @Test
    public void update_non_existent_account() {

        String nonExistentAccountName = "NonExistentAccount";
        Account updatedAccount = new Account(nonExistentAccountName, new BigDecimal("500.00"), "GBP");
        when(accountService.editAccount(eq(nonExistentAccountName), any(Account.class)))
                .thenThrow(new NotFoundException(Account.class, nonExistentAccountName));

        assertThrows(NotFoundException.class, () -> accountController.updateAccount(nonExistentAccountName, updatedAccount));
        verify(accountService, times(1)).editAccount(nonExistentAccountName, updatedAccount);
    }


    @Test
    public void test_delete_specific_account_success() {

        String accountName = "TestAccount";
        Account account = new Account(accountName, new BigDecimal("500.00"), "USD");
        when(accountService.getAccount(accountName)).thenReturn(account);

        ResponseEntity<?> response = accountController.deleteAccount(accountName);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(accountService, times(1)).deleteAccount(accountName);
    }

    @Test
    public void delete_non_existent_account() {

        String nonExistentAccountName = "NonExistentAccount";
        doThrow(NotFoundException.class).when(accountService).deleteAccount(nonExistentAccountName);

        assertThrows(NotFoundException.class, () -> accountController.deleteAccount(nonExistentAccountName));
        verify(accountService, times(1)).deleteAccount(nonExistentAccountName);
    }

    @Test
    public void delete_account_associated_with_transactions() {

        String accountName = "TestAccount";
        Account account = new Account(accountName, new BigDecimal("500.00"), "USD");
        when(accountService.getAccount(accountName)).thenReturn(account);

        Transaction transaction = new Transaction(3L, Type.DEBIT, account, "Test description", new BigDecimal(300), "EUR");
        when(transactionService.getTransaction(3L)).thenReturn(transaction);
        doThrow(AccountCannotBeDeletedException.class).when(accountService).deleteAccount(accountName);

        assertThrows(AccountCannotBeDeletedException.class, () -> accountController.deleteAccount(accountName));
        verify(accountService, times(1)).deleteAccount(accountName);
    }

    @Test
    public void test_delete_all_accounts_successfully() {

        doNothing().when(accountService).deleteAllAccounts();
        ResponseEntity<?> response = accountController.deleteAllAccounts();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }



}
