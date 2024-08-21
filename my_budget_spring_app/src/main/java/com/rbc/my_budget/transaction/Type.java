package com.rbc.my_budget.transaction;

public enum Type {
    CREDIT, DEBIT;

    public Type getOpposite() {
        return this == CREDIT ? DEBIT : CREDIT;
    }
}
