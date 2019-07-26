package at.uibk.charts.complexChart.chartEntries;

import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRates;
import at.uibk.model.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.stream.Collectors;

public class ExchangeRatesChartEntry extends ChartEntry<ExchangeRates> {
    public static final List<String> VALUE_TYPES = Arrays.stream(Currency.values())
            .map(currency -> currency.toString())
            .collect(Collectors.toList());
    private static final int SCALE = 4;

    public ExchangeRatesChartEntry(NavigableMap<LocalDate, ExchangeRates> timeSeries) {
        setId("exchange rate");
        setTimeSeries(timeSeries);
        setScale(SCALE);
    }

    /* application logic */

    @Override
    protected BigDecimal extractValue(ExchangeRates exchangeRates, String valueType) {
        if (exchangeRates == null || valueType == null)
            return null;

        Currency currency;
        try {
            currency = Currency.valueOf(valueType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(valueType + " is not supported!");
        }

        if (currency == Currency.USD) {
            return normalize(exchangeRates.getRate(Currency.USD));
        }

        if (currency == Currency.EUR)
            return normalize(exchangeRates.getRate(Currency.EUR));

        if (currency == Currency.CHF)
            return normalize(exchangeRates.getRate(Currency.CHF));

        return normalize(exchangeRates.getRate(Currency.GBP));
    }

}

