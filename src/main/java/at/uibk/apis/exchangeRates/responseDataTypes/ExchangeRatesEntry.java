package at.uibk.apis.exchangeRates.responseDataTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExchangeRatesEntry {
    private final LocalDate date;
    private final BigDecimal rate;

    public ExchangeRatesEntry(LocalDate date, BigDecimal rate) {
        this.date = date;
        this.rate = rate;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getRate() {
        return rate;
    }
}
