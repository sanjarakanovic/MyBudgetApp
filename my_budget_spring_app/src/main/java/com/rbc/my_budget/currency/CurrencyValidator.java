package com.rbc.my_budget.currency;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyValidator.class);
    private static final String CURRENCY_API_URL = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies.json";
    private final Set<String> validCurrencies = new HashSet<>();

    @Override
    public void initialize(ValidCurrency constraintAnnotation) {
        loadValidCurrencies();
    }

    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        return validCurrencies.contains(currency != null ? currency.toUpperCase() : null);
    }

    private void loadValidCurrencies() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            Map<?, ?> response = restTemplate.getForObject(CURRENCY_API_URL, Map.class);

            if (response != null) {
                for (Map.Entry<?, ?> entry : response.entrySet()) {
                    if (entry.getKey() instanceof String) {
                        validCurrencies.add(((String) entry.getKey()).toUpperCase());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error loading currencies.", e);
        }
    }
}
