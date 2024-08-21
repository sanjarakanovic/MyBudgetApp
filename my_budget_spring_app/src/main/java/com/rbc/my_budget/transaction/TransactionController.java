package com.rbc.my_budget.transaction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions(){
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping(path="{accountName}")
    public ResponseEntity<List<Transaction>> getAllTransactionsForAccount(@PathVariable("accountName") String accountName){
        return ResponseEntity.ok(transactionService.getTransactionsForAccount(accountName));
    }

    @GetMapping(path = "/transaction/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable("id") Long id){
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@Valid  @RequestBody Transaction newTransaction) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(newTransaction));
    }


    @PutMapping(path = "/transaction/{id}")
    public ResponseEntity<?> updateTransaction( @PathVariable("id") Long id,
                            @Valid @RequestBody Transaction updatedTransaction) {
        return ResponseEntity.ok(transactionService.editTransaction(id, updatedTransaction));
    }


    @DeleteMapping(path = "/transaction/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable("id") Long id) {
            transactionService.deleteTransaction(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllTransactions() {
        transactionService.deleteAllTransactions();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
