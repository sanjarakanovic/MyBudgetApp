package com.rbc.my_budget.account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts(){
        return ResponseEntity.ok(accountService.getAllAccounts());
    }


    @GetMapping(path = "/account/{name}")
    public ResponseEntity<Account> getAccount(@PathVariable("name") String name){
        return ResponseEntity.ok(accountService.getAccount(name));
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account newAccount) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(newAccount));
    }


    @PutMapping(path = "/account/{name}")
    public ResponseEntity<Account> updateAccount(@PathVariable("name") String name,
                                           @Valid @RequestBody Account updatedAccount) {
        return ResponseEntity.ok(accountService.editAccount(name, updatedAccount));
    }


    @DeleteMapping(path = "/account/{name}")
    public ResponseEntity<?> deleteAccount(@PathVariable("name") String name) {
        accountService.deleteAccount(name);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAllAccounts() {
        accountService.deleteAllAccounts();
        return ResponseEntity.noContent().build();
    }
}
