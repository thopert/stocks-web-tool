package at.uibk.model.factories;

import at.uibk.model.Currency;
import at.uibk.model.mainEntities.ExchangeRateDirection;
import at.uibk.model.mainEntities.Holding;
import at.uibk.model.mainEntities.Trade;
import at.uibk.model.mainEntities.TradeType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TradeFactory {

    public static Trade getDefaultAdjustment(Holding holding) {
        LocalDate date = LocalDate.now();
        TradeType type = TradeType.SPLIT;
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal price = BigDecimal.ZERO;
        BigDecimal exchangeRate = BigDecimal.ZERO;
        ExchangeRateDirection exchangeRateDirection = ExchangeRateDirection.DEFAULT;
        BigDecimal brokerage = BigDecimal.ZERO;
        Currency brokerageCurrency = holding.getCurrency();

        return new Trade (date, type, quantity, price, exchangeRate, exchangeRateDirection, brokerage, brokerageCurrency,
                holding);
    }

    public static Trade getDefaultTrade(TradeType type, Holding holding) {
        LocalDate date = LocalDate.now();
        BigDecimal quantity = BigDecimal.ONE;
        BigDecimal price = BigDecimal.ZERO;
        BigDecimal exchangeRate = BigDecimal.ONE;
        ExchangeRateDirection exchangeRateDirection = ExchangeRateDirection.DEFAULT;
        BigDecimal brokerage = BigDecimal.ZERO;
        Currency brokerageCurrency = holding.getCurrency();

        return new Trade(date, type, quantity, price, exchangeRate, exchangeRateDirection, brokerage,
                brokerageCurrency, holding);
    }


    public static Trade getAdjustment(LocalDate date, BigDecimal quantity, TradeType type, Holding holding) {
        return new Trade(date, type, quantity, BigDecimal.ZERO, BigDecimal.ZERO, ExchangeRateDirection.DEFAULT,
                BigDecimal.ZERO, holding.getCurrency(), holding);
    }
}
