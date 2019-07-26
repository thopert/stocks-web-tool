package at.uibk.apis.exchangeRates.responseDataTypes;

import at.uibk.model.Currency;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class ExchangeRatesTimeSeries implements Serializable {
    private LocalDate start;
    private LocalDate end;
    private Currency base;
    private Map<LocalDate, Map<Currency, BigDecimal>> rates;

    public ExchangeRatesTimeSeries(LocalDate start, LocalDate end, Currency base,
                                   Map<LocalDate, Map<Currency, BigDecimal>> rates) {
        this.start = start;
        this.end = end;
        this.base = base;
        this.rates = rates;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public Currency getBase() {
        return base;
    }

    public Map<LocalDate, Map<Currency, BigDecimal>> getRates() {
        return rates;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public void setBase(Currency base) {
        this.base = base;
    }

    public void setRates(Map<LocalDate, Map<Currency, BigDecimal>> rates) {
        this.rates = rates;
    }
}
