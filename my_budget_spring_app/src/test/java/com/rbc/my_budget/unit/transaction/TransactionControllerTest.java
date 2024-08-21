package com.rbc.my_budget.unit.transaction;

import com.rbc.my_budget.account.Account;
import com.rbc.my_budget.exception.NotFoundException;
import com.rbc.my_budget.transaction.Transaction;
import com.rbc.my_budget.transaction.TransactionController;
import com.rbc.my_budget.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TransactionControllerTest {

    @InjectMocks
    private TransactionController transactionController;

    @Mock
    private TransactionService transactionService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_retrieve_all_transactions_successfully() {

        List<Transaction> transactions = List.of(new Transaction(), new Transaction());
        when(transactionService.getAllTransactions()).thenReturn(transactions);

        ResponseEntity<List<Transaction>> response = transactionController.getAllTransactions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(transactions, response.getBody());
    }

    @Test
    public void test_retrieve_transactions_for_non_existent_account() {

        String nonExistentAccountName = "nonExistentAccount";
        when(transactionService.getTransactionsForAccount(nonExistentAccountName))
            .thenThrow(new NotFoundException(Account.class, nonExistentAccountName));

        assertThrows(NotFoundException.class, () -> transactionController.getAllTransactionsForAccount(nonExistentAccountName));
    }

    @Test
    public void test_retrieve_transactions_for_specific_account_successfully() {

        List<Transaction> transactions = List.of(new Transaction(), new Transaction());
        String accountName = "TestAccount";
        when(transactionService.getTransactionsForAccount(accountName)).thenReturn(transactions);

        ResponseEntity<List<Transaction>> response = transactionController.getAllTransactionsForAccount(accountName);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(transactions, response.getBody());
    }

    @Test
    public void test_retrieve_specific_transaction_successfully() {

        Long transactionId = 1L;
        Transaction expectedTransaction = new Transaction();
        when(transactionService.getTransaction(transactionId)).thenReturn(expectedTransaction);

        ResponseEntity<Transaction> response = transactionController.getTransaction(transactionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedTransaction, response.getBody());
    }

    @Test
    public void test_retrieve_transaction_non_existent_id() {
        Long nonExistentId = 999L;

        doThrow(NotFoundException.class).when(transactionService).deleteTransaction(nonExistentId);

        assertThrows(NotFoundException.class, () -> transactionController.deleteTransaction(nonExistentId));
        verify(transactionService, times(1)).deleteTransaction(nonExistentId);
    }

    @Test
    public void test_create_new_transaction_successfully() {

        Transaction newTransaction = new Transaction();
        when(transactionService.createTransaction(any(Transaction.class))).thenReturn(newTransaction);

        ResponseEntity<?> response = transactionController.createTransaction(newTransaction);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newTransaction, response.getBody());
    }

    @Test
    public void update_existing_transaction_successfully() {

        Long id = 1L;
        Transaction updatedTransaction = new Transaction();
        Transaction existingTransaction = new Transaction();
        when(transactionService.editTransaction(eq(id), any(Transaction.class))).thenReturn(existingTransaction);

        ResponseEntity<?> response = transactionController.updateTransaction(id, updatedTransaction);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(existingTransaction, response.getBody());
    }

    @Test
    public void update_transaction_with_non_existent_id() {

        Long nonExistentId = 999L;
        Transaction updatedTransaction = new Transaction();
        when(transactionService.editTransaction(eq(nonExistentId), any(Transaction.class)))
                .thenThrow(new NotFoundException(Transaction.class, nonExistentId));

        assertThrows(NotFoundException.class, () -> transactionController.updateTransaction(nonExistentId, updatedTransaction));
        verify(transactionService, times(1)).editTransaction(nonExistentId, updatedTransaction);
    }


    @Test
    public void test_delete_transaction_by_id_successfully() {

        Long transactionId = 1L;
        Transaction transaction = new Transaction();
        when(transactionService.getTransaction(transactionId)).thenReturn(transaction);

        ResponseEntity<?> response = transactionController.deleteTransaction(transactionId);

        verify(transactionService, times(1)).deleteTransaction(transactionId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void test_delete_transaction_non_existent_id() {

        Long nonExistentId = 999L;
        doThrow(new NotFoundException(Transaction.class, nonExistentId)).when(transactionService).deleteTransaction(nonExistentId);

        assertThrows(NotFoundException.class, () -> transactionController.deleteTransaction(nonExistentId));
        verify(transactionService, times(1)).deleteTransaction(nonExistentId);
    }

    @Test
    public void delete_all_transactions_successfully() {

        List<Transaction> transactions = List.of(new Transaction(), new Transaction());
        when(transactionService.getAllTransactions()).thenReturn(transactions);

        ResponseEntity<Void> response = transactionController.deleteAllTransactions();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(transactionService, times(1)).deleteAllTransactions();
    }

}