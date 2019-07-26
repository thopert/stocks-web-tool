package at.uibk.model.mainEntities;

import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRates;
import at.uibk.model.Currency;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

// Standard base is EUR

@Entity
@Table(name = "exchangeRate")
public class ExchangeRatesEntity implements Serializable {
    private static Currency DEFAULT_BASE = Currency.EUR;

    @Id
    private LocalDate date;

    //  private BigDecimal EUR = BigDecimal.ONE;
    private BigDecimal GBP;
    private BigDecimal USD;
    private BigDecimal CHF;

    protected ExchangeRatesEntity(){}

    public static ExchangeRatesEntity of(LocalDate date, Map<Currency, BigDecimal> currencyToRate){
        return new ExchangeRatesEntity(date, currencyToRate);
    }

    public ExchangeRatesEntity(LocalDate date, Map<Currency, BigDecimal> currencyToRate) {
        this.date = date;

        GBP = currencyToRate.get(Currency.GBP);
        USD = currencyToRate.get(Currency.USD);
        CHF = currencyToRate.get(Currency.CHF);
    }

    private Map<Currency, BigDecimal> getRates() {
        Map<Currency, BigDecimal> ratesMap = new HashMap<>();

        ratesMap.put(DEFAULT_BASE, BigDecimal.ONE);
        ratesMap.put(Currency.GBP, GBP);
        ratesMap.put(Currency.USD, USD);
        ratesMap.put(Currency.CHF, CHF);

        return ratesMap;
    }

    public LocalDate getDate() {
        return date;
    }

    public ExchangeRates toExchangeRates() {
        return toExchangeRates(DEFAULT_BASE);
    }

    public ExchangeRates toExchangeRates(Currency baseCurrency){
        ExchangeRates exchangeRates = new ExchangeRates(DEFAULT_BASE, date, getRates());
        exchangeRates.resetBase(baseCurrency);
        return exchangeRates;
    }
}
