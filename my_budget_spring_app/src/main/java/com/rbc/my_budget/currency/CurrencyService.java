package com.rbc.my_budget.currency;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrencyService {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

    private final RestTemplate restTemplate;
    private static final String EXCHANGE_API_URL = "https://latest.currency-api.pages.dev/v1/currencies/";

    public BigDecimal getExchangeRate(String baseCurrency, String targetCurrency) {

        String url = UriComponentsBuilder.fromHttpUrl(EXCHANGE_API_URL+ baseCurrency.toLowerCase()+ ".json")
                .toUriString();

        CurrencyRateResponse response = restTemplate.getForObject(url, CurrencyRateResponse.class);

        if (response != null) {
            Map<String, BigDecimal> targetRates = response.getRates().get(baseCurrency.toLowerCase());

            if (targetRates != null)
                return targetRates.get(targetCurrency.toLowerCase());
            else
                logger.error("No rates found for base currency: {} in response for URL: {}", baseCurrency, url);
        } else
            logger.error("Received null response from API for URL: {}", url);

        return null;
    }


}
