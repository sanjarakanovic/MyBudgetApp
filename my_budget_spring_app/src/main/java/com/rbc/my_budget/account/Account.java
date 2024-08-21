package com.rbc.my_budget.account;


import com.rbc.my_budget.currency.ValidCurrency;
import com.rbc.my_budget.transaction.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Account {

    @NotBlank(message = "Name cannot be blank.")
    @JacksonXmlProperty(localName = "name")
    @Id
    private String name;


    @NotNull(message = "Balance cannot be null.")
    @JacksonXmlProperty(localName = "Balance")
    @Column (nullable = false)
    private BigDecimal balance;

    @ValidCurrency
    @JacksonXmlProperty(localName = "currency")
    @Column (nullable = false)
    private String currency;

    @JacksonXmlElementWrapper(localName = "Transactions")
    @JacksonXmlProperty(localName = "Transaction")
    @Transient
    private List<Transaction> transactions;


    public Account(String name, BigDecimal balance, String currency){
        this.name= name;
        this.balance= balance;
        this.currency= currency;
    }
}
