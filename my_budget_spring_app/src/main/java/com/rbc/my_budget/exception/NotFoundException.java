package com.rbc.my_budget.exception;

public class NotFoundException extends RuntimeException{
    public NotFoundException(Class<?> entityClass, Object id) {
        super(String.format("%s not found with ID: %s", entityClass.getSimpleName(), id.toString()));
    }
}
