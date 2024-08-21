package com.rbc.my_budget.exception;

public class AccountCannotBeDeletedException extends RuntimeException{

    public AccountCannotBeDeletedException() {
        super("Account cannot be deleted since it has transactions associated with it.");
    }
}
