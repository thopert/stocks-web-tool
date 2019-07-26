package at.uibk.controller.holdings;


import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRates;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeries;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeriesEntry;
import at.uibk.model.Currency;
import at.uibk.model.factories.TradeFactory;
import at.uibk.model.mainEntities.Dividend;
import at.uibk.model.mainEntities.Holding;
import at.uibk.model.mainEntities.Trade;
import at.uibk.model.mainEntities.TradeType;
import at.uibk.services.apis.AlphaVantageService;
import at.uibk.services.beans.DividendBean;
import at.uibk.services.beans.ExchangeRatesBean;
import at.uibk.services.beans.TradeBean;
import at.uibk.utils.TradeUtils;

import javax.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

class SplitsAndDividendFetcher implements Serializable {
    @Inject
    private AlphaVantageService alphaVantageService;
    @Inject
    private ExchangeRatesBean exchangeRatesBean;
    @Inject
    private DividendBean dividendBean;
    @Inject
    private TradeBean tradeBean;

    public void setupDividends(Holding holdingWithTradesAndDividends){
        List<Dividend> dividends = holdingWithTradesAndDividends.getDividends();

        StockTimeSeries stockTimeSeries =
                alphaVantageService.getTimeSeriesDaily(holdingWithTradesAndDividends.getFullyQualifiedSymbol());

        TreeMap<LocalDate, Dividend> dividendsMap = new TreeMap<>();

        dividends.forEach(dividend -> dividendsMap.put(dividend.getDate(), dividend));

        LocalDate openingDate = holdingWithTradesAndDividends.getFirstTradeDate();

        Collection<StockTimeSeriesEntry> stockTimeSeriesEntries = stockTimeSeries.getEntries().tailMap(openingDate).values();

        for (StockTimeSeriesEntry stockTimeSeriesEntry : stockTimeSeriesEntries) {
            if(stockTimeSeriesEntry.hasDividend()){
                LocalDate dividendDate = stockTimeSeriesEntry.getDate();
                if(!dividendsMap.containsKey(dividendDate)){
                    Currency portfolioCurrency = holdingWithTradesAndDividends.getPortfolio().getCurrency();
                    ExchangeRates exchangeRates = exchangeRatesBean.getFloorEntry(dividendDate, portfolioCurrency);
                    BigDecimal exchangeRate = exchangeRates.getRate(holdingWithTradesAndDividends.getCurrency());
                    BigDecimal amount = stockTimeSeriesEntry.getDividendAmount();
                    Dividend dividend = new Dividend(dividendDate, amount, exchangeRate, holdingWithTradesAndDividends);
                    dividendBean.persist(dividend);
                    dividends.add(dividend);
                }
            }
        }
    }

    public void setupSplits(Holding holdingWithTradesAndDividends){
        StockTimeSeries stockTimeSeries =
                alphaVantageService.getTimeSeriesDaily(holdingWithTradesAndDividends.getFullyQualifiedSymbol());

        TreeMap<LocalDate, Trade> adjustmentMap = new TreeMap<>();

        List<Trade> trades = holdingWithTradesAndDividends.getTrades();

        List<Trade> adjustments = trades.stream()
                .filter(Trade::isAdjustment)
                .collect(Collectors.toList());

        adjustments.forEach(adjustment -> adjustmentMap.put(adjustment.getDate(), adjustment));

        LocalDate openingDate = holdingWithTradesAndDividends.getFirstTradeDate();

        Collection<StockTimeSeriesEntry> stockTimeSeriesEntries = stockTimeSeries.getEntries().tailMap(openingDate).values();

        for (StockTimeSeriesEntry stockTimeSeriesEntry : stockTimeSeriesEntries) {
            if(stockTimeSeriesEntry.isAdjustment()){
                LocalDate adjustmentDate = stockTimeSeriesEntry.getDate();
                if(!adjustmentMap.containsKey(adjustmentDate)){
                    Trade adjustment = handleAdjustment(stockTimeSeriesEntry, holdingWithTradesAndDividends);
                    trades.add(adjustment);
                }
            }
        }
    }

    private Trade handleAdjustment(StockTimeSeriesEntry splitEntry, Holding holdingWithTradesAndDividends){
        LocalDate splitDate = splitEntry.getDate();

        List<Trade> tradesUntilSplit = holdingWithTradesAndDividends.getTradesUntil(splitDate);

        BigDecimal numberOfShares = TradeUtils.getNumberOfShares(tradesUntilSplit);

        BigDecimal splitCoefficent = splitEntry.getSplitCoefficient();

        BigDecimal amountAfterSplit = numberOfShares.multiply(splitCoefficent);

        BigDecimal additionalShares = amountAfterSplit.subtract(numberOfShares);

        TradeType type = additionalShares.compareTo(BigDecimal.ZERO) <= 0 ? TradeType.CONSOLIDATION : TradeType.SPLIT;

        Trade adjustment = TradeFactory.getAdjustment(splitDate, additionalShares.abs(), type,
                holdingWithTradesAndDividends);

        tradeBean.persist(adjustment);

        return adjustment;

    }

}
