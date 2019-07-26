package at.uibk.controller.holdings;

import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRates;
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

import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

class TradesAndDividendsManager implements Serializable {
    @EJB
    private TradeBean tradeBean;
    @EJB
    private DividendBean dividendBean;
    @Inject
    private AlphaVantageService alphaVantageService;
    @EJB
    private ExchangeRatesBean exchangeRatesBean;

    private List<Trade> trades;

    private List<Dividend> dividends;

    private Holding selectedHolding;

    public void init(Holding holdingEager) {
        this.selectedHolding = holdingEager;
        this.trades = holdingEager.getTrades();
        this.dividends = holdingEager.getDividends();
    }

    /* trade actions */

    public boolean persist(Trade tradeToPersist) {
        if (!TradeUtils.isPersistCorrect(tradeToPersist, trades)) {
            return false;
        }

        tradeBean.persistNoChecks(tradeToPersist);
        trades.add(tradeToPersist);
        return true;
    }

    public boolean mergeTrade(Trade tradeToMerge) {
        if (!TradeUtils.isMergeCorrect(tradeToMerge, trades)) {
//            restoreTrade(tradeToMerge);
            return false;
        }

        tradeBean.mergeNoChecks(tradeToMerge);

        replaceOld(tradeToMerge);

        return true;
    }

    private void replaceOld(Trade trade) {
        int indexOfOldVersion = 0;
        while (indexOfOldVersion < trades.size()) {
            if (trades.get(indexOfOldVersion).getId() == trade.getId())
                break;

            indexOfOldVersion++;
        }

        trades.set(indexOfOldVersion, trade);
    }

    public boolean removeTrade(Trade tradeToRemove) {
        if (!TradeUtils.isRemoveCorrect(tradeToRemove, trades)) {
            return false;
        }

        tradeBean.removeNoChecks(tradeToRemove);
        trades.remove(tradeToRemove); // sorting is not changed
        return true;
    }

//    private void restoreTrade(Trade trade) {
//        trades.set(trades.indexOf(trade), tradeBean.find(trade.getId()));
//    }

    public void persistAdjustments(List<Trade> adjustments) {
        for (Trade adjustment : adjustments) {
            tradeBean.persist(adjustment);
            trades.add(adjustment);
        }
    }

    public Optional<LocalDate> getMinDate() {
        return trades.stream().
                map(Trade::getDate).
                min(Comparator.naturalOrder());
    }

    public List<Trade> getTrades() {
        return trades;
    }

    /* dividend actions */

    public void persist(Dividend divided) {
        dividendBean.persist(divided);
        dividends.add(divided);
    }

    public void merge(Dividend dividend) {
        dividendBean.merge(dividend);
        replaceOld(dividend);
    }

    private void replaceOld(Dividend dividend) {
        int indexOfOldVersion = 0;
        while (indexOfOldVersion < dividends.size()) {
            if (dividends.get(indexOfOldVersion).getId() == dividend.getId())
                break;

            indexOfOldVersion++;
        }

        dividends.set(indexOfOldVersion, dividend);
    }

    public void remove(Dividend dividend) {
        dividendBean.remove(dividend);
        dividends.remove(dividend);
    }

    public void persistDividends(List<Dividend> dividends) {
        for (Dividend dividend : dividends) {
            dividendBean.persist(dividend);
            this.dividends.add(dividend);
        }
    }

    public List<Dividend> getDividends() {
        return dividends;
    }

    /* missing dividends and splits checker */

    public boolean hasMissingDividends() {
        Set<LocalDate> relevantDividendDates = alphaVantageService.getDividendTimeSeries(selectedHolding.getFullyQualifiedSymbol())
                        .tailMap(selectedHolding.getFirstTradeDate(), true)
                        .keySet();

        List<LocalDate> storedDividendDates = dividends.stream().map(Dividend::getDate).collect(Collectors.toList());

        return relevantDividendDates.stream().anyMatch(date -> !storedDividendDates.contains(date));
    }


    public boolean hasMissingAdjustments() {
        Set<LocalDate> relevantAdjustmentDates = alphaVantageService.getSplitTimeSeries(selectedHolding.getFullyQualifiedSymbol())
                .tailMap(selectedHolding.getFirstTradeDate(), true)
                .keySet();

        List<LocalDate> storedAdjustmentDates = trades.stream().filter(Trade::isAdjustment).map(Trade::getDate).collect(Collectors.toList());

        return relevantAdjustmentDates.stream().anyMatch(date -> !storedAdjustmentDates.contains(date));
    }

    /* missing dividends  */

    public List<Dividend> getMissingDividends() {
        Collection<StockTimeSeriesEntry> relevantDividends = alphaVantageService.getDividendTimeSeries(selectedHolding.getFullyQualifiedSymbol())
                .tailMap(selectedHolding.getFirstTradeDate(), true)
                .values();

        List<LocalDate> storedDividendDates = dividends.stream().map(Dividend::getDate).collect(Collectors.toList());

        List<Dividend> missingDividends = new ArrayList<>();

        long dividendIdCounter = 0;
        for (StockTimeSeriesEntry dividendEntry : relevantDividends) {

            if (storedDividendDates.contains(dividendEntry.getDate()))
                continue;

            Dividend dividendToAdd = createDividend(dividendEntry);

            dividendToAdd.setId(dividendIdCounter++);

            missingDividends.add(dividendToAdd);
        }

        return missingDividends;
    }

    private Dividend createDividend(StockTimeSeriesEntry dividendEntry) {
        LocalDate dividendDate = dividendEntry.getDate();

        Currency portfolioCurrency = selectedHolding.getPortfolio().getCurrency();

        ExchangeRates exchangeRates = exchangeRatesBean.getFloorEntry(dividendDate, portfolioCurrency);

        BigDecimal exchangeRate = exchangeRates.getRate(selectedHolding.getCurrency());

        BigDecimal amount = dividendEntry.getDividendAmount();

        return new Dividend(dividendDate, amount, exchangeRate, selectedHolding);
    }


    /* missing adjustments  */

    public List<Trade> getMissingAdjustments() {
        Collection<StockTimeSeriesEntry> relevantAdjustments = alphaVantageService.getSplitTimeSeries(selectedHolding.getFullyQualifiedSymbol())
                .tailMap(selectedHolding.getFirstTradeDate(), true)
                .values();

        List<LocalDate> storedAdjustmentDates = trades.stream()
                .filter(Trade::isAdjustment)
                .map(Trade::getDate).collect(Collectors.toList());

        List<Trade> missingAdjustments = new ArrayList<>();
        long adjustmentIdCounter = 0;

        for (StockTimeSeriesEntry adjustmentEntry : relevantAdjustments) {

            if (storedAdjustmentDates.contains(adjustmentEntry.getDate()))
                continue;

            Trade adjustment = createAdjustment(adjustmentEntry);
            adjustment.setId(adjustmentIdCounter++);
            missingAdjustments.add(adjustment);
        }

        return missingAdjustments;
    }

    private Trade createAdjustment(StockTimeSeriesEntry adjustmentEntry) {
        LocalDate splitDate = adjustmentEntry.getDate();

        List<Trade> tradesUntilSplit = selectedHolding.getTradesUntil(splitDate);

        BigDecimal numberOfShares = TradeUtils.getNumberOfShares(tradesUntilSplit);

        BigDecimal splitCoefficent = adjustmentEntry.getSplitCoefficient();

        BigDecimal amountAfterSplit = numberOfShares.multiply(splitCoefficent);

        BigDecimal additionalShares = amountAfterSplit.subtract(numberOfShares);

        TradeType type = additionalShares.compareTo(BigDecimal.ZERO) <= 0 ? TradeType.CONSOLIDATION : TradeType.SPLIT;

        Trade adjustment = TradeFactory.getAdjustment(splitDate, additionalShares.abs(), type,
                selectedHolding);

        return adjustment;
    }
}
