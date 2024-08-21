package com.rbc.my_budget.account;

import com.rbc.my_budget.exception.AccountAlreadyExistsException;
import com.rbc.my_budget.exception.AccountCannotBeDeletedException;
import com.rbc.my_budget.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public List<Account> getAllAccounts(){
        return accountRepository.findAll();
    }

    public Account getAccount(String name){
        return accountRepository.findById(name)
                .orElseThrow(() -> new NotFoundException(Account.class, name));
    }

    public Account createAccount(Account account){
        if(accountRepository.existsById(account.getName()))
            throw new AccountAlreadyExistsException(account.getName());
        return accountRepository.save(account);
    }


    public Account editAccount(String name, Account updatedAccount){

        Account existingAccount = accountRepository.findById(name)
                .orElseThrow(() -> new NotFoundException(Account.class, name));

        existingAccount.setBalance(updatedAccount.getBalance());
        existingAccount.setCurrency(updatedAccount.getCurrency());

        return accountRepository.save(existingAccount);
    }

    public void deleteAccount(String name) {

        Account account = accountRepository.findById(name)
                .orElseThrow(() -> new NotFoundException(Account.class, name));
        try {
            accountRepository.delete(account);
        }catch (DataIntegrityViolationException e) {
            throw new AccountCannotBeDeletedException();
        }
    }


    @Transactional
    public void deleteAllAccounts() {
        try {
            accountRepository.deleteAll();
        } catch (DataIntegrityViolationException e) {
            throw new AccountCannotBeDeletedException();
        }
    }
}
