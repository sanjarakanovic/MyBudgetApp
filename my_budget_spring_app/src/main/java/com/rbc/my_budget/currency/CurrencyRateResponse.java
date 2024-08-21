package com.rbc.my_budget.currency;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Data
public class CurrencyRateResponse {

    private String date;
    private Map<String, Map<String, BigDecimal>> rates = new HashMap<>();

    @JsonAnySetter
    public void setCurrency(String key, Map<String, BigDecimal> value) {
        rates.put(key, value);
    }
}
