package at.uibk.apis.exchangeRates.responseDataTypes;

import at.uibk.model.Currency;
import at.uibk.model.mainEntities.ExchangeRateDirection;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

public class ExchangeRates implements Serializable {
    private static final int DECIMAL_PLACES = 4;

    private Currency base;

    private LocalDate date;

    private Map<Currency, BigDecimal> rates;

    public ExchangeRates(Currency base, LocalDate date, Map<Currency, BigDecimal> exchangeRates) {
        this.base = base;
        this.date = date;
        this.rates = exchangeRates;
    }

    public ExchangeRatesEntry getRateEntry(Currency currency, ExchangeRateDirection exchangeRateDirection) {
        return new ExchangeRatesEntry(date, getRate(currency, exchangeRateDirection));
    }

    public ExchangeRatesEntry getRateEntry(Currency currency) {
        return new ExchangeRatesEntry(date, getRate(currency));
    }

    public ExchangeRatesEntry getReverseRateEntry(Currency currency) {
        return new ExchangeRatesEntry(date, getReverseRate(currency));
    }

    public BigDecimal getRate(Currency currency, ExchangeRateDirection exchangeRateDirection){
        if(exchangeRateDirection == ExchangeRateDirection.DEFAULT)
            return getRate(currency);

        return getReverseRate(currency);
    }

    public BigDecimal getRate(Currency currency) {
        return rates.get(currency);
    }

    public BigDecimal getReverseRate(Currency currency) {
        BigDecimal portfolioToForeign = rates.get(currency);
        return BigDecimal.ONE.divide(portfolioToForeign, DECIMAL_PLACES, RoundingMode.HALF_EVEN);
    }

    public LocalDate getDate() {
        return date;
    }

    public Currency getBase() {
        return base;
    }

    public Map<Currency, BigDecimal> getRates() {
        return rates;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setBase(Currency base) {
        this.base = base;
    }

    public void setRates(TreeMap<Currency, BigDecimal> rates) {
        this.rates = rates;
    }

    public void resetBase(Currency base) {
        if (this.base == base) {
            return;
        }

        BigDecimal referenceRate = rates.get(base);

        if (referenceRate == null) {
            throw new IllegalArgumentException();
        }

        this.base = base;

        for (Map.Entry<Currency, BigDecimal> rateEntry : rates.entrySet()) {
            BigDecimal oldRate = rateEntry.getValue();
            if (oldRate != null) {
                BigDecimal transformedRate = oldRate.divide(referenceRate, DECIMAL_PLACES, BigDecimal.ROUND_HALF_EVEN);
                rates.put(rateEntry.getKey(), transformedRate);
            }
        }
    }
}
