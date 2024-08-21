package com.rbc.my_budget.transaction;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import com.rbc.my_budget.account.Account;
import com.rbc.my_budget.currency.ValidCurrency;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Type cannot be null.")
    @Column (nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @NotNull(message = "Account cannot be null.")
    @ManyToOne
    @JoinColumn(name = "account_name", nullable = false)
    private Account account;

    @JacksonXmlProperty(localName = "Description")
    @NotBlank(message = "Description cannot be blank.")
    @Size(max = 30, message = "Description cannot be longer than 30 characters.")
    @Column (nullable = false)
    private String description;

    @NotNull(message = "Amount cannot be null.")
    @Positive(message = "Amount must be positive.")
    @Column (nullable = false)
    private BigDecimal amount;


    @ValidCurrency
    @Column (nullable = false)
    private String currency;

    @JacksonXmlProperty(localName = "Amount")
    public void setAmountElement(AmountElement amountElement) {
        this.currency = amountElement.currency;
        this.amount = amountElement.value;
    }
    public static class AmountElement {
        @JacksonXmlProperty(isAttribute = true)
        public String currency;

        @JacksonXmlText
        public BigDecimal value;
    }
}
