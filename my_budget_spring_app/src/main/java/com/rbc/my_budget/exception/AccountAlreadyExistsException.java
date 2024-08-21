package com.rbc.my_budget.exception;

public class AccountAlreadyExistsException extends RuntimeException{

    public AccountAlreadyExistsException(String name) {
        super("Account with name " + name + " already exists.");
    }
}
