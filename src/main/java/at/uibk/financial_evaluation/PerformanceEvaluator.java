package at.uibk.financial_evaluation;

import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRates;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeries;
import at.uibk.model.Currency;
import at.uibk.model.mainEntities.*;
import at.uibk.services.apis.AlphaVantageService;
import at.uibk.services.beans.ExchangeRatesBean;
import at.uibk.utils.TradeUtils;

import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PerformanceEvaluator implements Serializable {
    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;
    @EJB
    private ExchangeRatesBean exchangeRatesBean;
    @Inject
    private AlphaVantageService alphaVantageService;

    /* Holding Performance */

    public HoldingPerformance getHoldingPerformance(Holding holdingEager) {

        return getHoldingPerformance(holdingEager, LocalDate.now());
    }

    private HoldingPerformance getHoldingPerformance(Holding holdingEager, LocalDate deadLine) {

        if (holdingEager == null || !holdingEager.hasTrades())
            throw new IllegalArgumentException("Holding is null or has no trades!");

        String fullyQualifiedSymbol = holdingEager.getFullyQualifiedSymbol();

        Currency holdingCurrency = holdingEager.getCurrency();

        Currency portfolioCurrency = holdingEager.getPortfolio().getCurrency();

        BigDecimal currentPrice = alphaVantageService.getFloorClose(fullyQualifiedSymbol, deadLine).getClose();

        BigDecimal currentExchangeRate = exchangeRatesBean.getFloorEntry(deadLine, portfolioCurrency).getRate(holdingCurrency);

        return calculateHoldingPerformance(holdingEager, currentPrice, currentExchangeRate, deadLine);
    }

    private HoldingPerformance calculateHoldingPerformance(Holding holdingEager, BigDecimal currentPrice,
                                                           BigDecimal currentExchangeRate, LocalDate deadLine) {
        List<Trade> tradesUntilDeadLine = holdingEager.getTradesUntil(deadLine);

        if (tradesUntilDeadLine.isEmpty()) // possible in portfolio time series
            return new HoldingPerformance(holdingEager);

        BigDecimal totalSales = BigDecimal.ZERO; // all sales + all current holdings
        BigDecimal totalCosts = BigDecimal.ZERO; // all buys with brokerage + brokerage of sales
        BigDecimal numberOfShares = BigDecimal.ZERO;

        for (Trade trade : tradesUntilDeadLine) {
            TradeType tradeType = trade.getType();

            BigDecimal convertedTradeTotal = trade.getWorthInPortfolioCurrency();

            BigDecimal convertedBrokerage = trade.getBrokerageInPortfolioCurrency();

            if (tradeType == TradeType.BUY) {
                numberOfShares = numberOfShares.add(trade.getQuantity());
                totalCosts = totalCosts.add(convertedTradeTotal).add(convertedBrokerage);

            } else if (tradeType == TradeType.SPLIT) {
                numberOfShares = numberOfShares.add(trade.getQuantity());

            } else if (tradeType == TradeType.SELL) {
                numberOfShares = numberOfShares.subtract(trade.getQuantity());
                totalSales = totalSales.add(convertedTradeTotal);
                totalCosts = totalCosts.add(convertedBrokerage);

            } else if (tradeType == TradeType.CONSOLIDATION) {
                numberOfShares = numberOfShares.subtract(trade.getQuantity());
            }
        }

        // shares in possession
        BigDecimal currentValue = currentPrice
                .multiply(numberOfShares)
                .divide(currentExchangeRate, SCALE, ROUNDING_MODE);

        BigDecimal totalDividends = getTotalDividendAmount(holdingEager, deadLine);

        BigDecimal totalEarnings = currentValue.add(totalSales).add(totalDividends);

        BigDecimal totalReturn = totalEarnings.subtract(totalCosts);

        BigDecimal totalPercentReturn = totalReturn.divide(totalCosts, SCALE, ROUNDING_MODE);

        return new HoldingPerformance(holdingEager, deadLine, currentPrice, currentExchangeRate,
                numberOfShares, currentValue, totalSales, totalDividends, totalEarnings, totalCosts, totalReturn, totalPercentReturn);
    }

    private BigDecimal getTotalDividendAmount(Holding holdingEager, LocalDate deadLine) {

        List<Dividend> dividendsUntilDeadLine = holdingEager.getDividendsUntil(deadLine);

        BigDecimal totalDividendAmount = BigDecimal.ZERO;

        for (Dividend dividend : dividendsUntilDeadLine) {

            LocalDate dividendDate = dividend.getDate();

            List<Trade> tradesUntilDividend = holdingEager.getTradesUntil(dividendDate);

            BigDecimal numberOfShares = TradeUtils.getNumberOfShares(tradesUntilDividend);

            BigDecimal currentDividendAmount = dividend.getAmountInPortfolioCurrency().multiply(numberOfShares);

            totalDividendAmount = totalDividendAmount.add(currentDividendAmount);
        }

        return totalDividendAmount;
    }

//    private TreeMap<LocalDate, HoldingPerformance> getHoldingPerformanceTimeSeries(Holding holdingEager, LocalDate startDate) {
//        if (startDate == null) {
//            throw new IllegalArgumentException("Date cannot be null!");
//        }
//
//        TreeMap<LocalDate, HoldingPerformance> holdingPerformanceTimeSeries = new TreeMap<>();
//
//        if (holdingEager == null || !holdingEager.hasTrades()) {
//            return holdingPerformanceTimeSeries;
//        }
//
//        while (startDate.compareTo(LocalDate.now()) <= 0) {
//            DayOfWeek dayOfWeek = startDate.getDayOfWeek();
//            if (!isWeekEnd(dayOfWeek)) {
//                holdingPerformanceTimeSeries.put(startDate, getHoldingPerformance(holdingEager, startDate));
//            }
//            startDate = startDate.plusDays(1);
//        }
//
//        return holdingPerformanceTimeSeries;
//    }

    private TreeMap<LocalDate, HoldingPerformance> getHoldingPerformanceTimeSeriesOptimized(Holding holdingEager, LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Date cannot be null!");
        }

        TreeMap<LocalDate, HoldingPerformance> holdingPerformanceTimeSeries = new TreeMap<>();

        if (holdingEager == null || !holdingEager.hasTrades()) {
            return holdingPerformanceTimeSeries;
        }

        Currency portfolioCurrency = holdingEager.getPortfolio().getCurrency();

        NavigableMap<LocalDate, ExchangeRates> exchangeRatesTimeSeries = exchangeRatesBean.getFloorTailMap(startDate,
                portfolioCurrency);

        StockTimeSeries stockTimeSeries = alphaVantageService.getTimeSeriesDaily(holdingEager.getFullyQualifiedSymbol());

        while (startDate.compareTo(LocalDate.now()) <= 0) {
            DayOfWeek dayOfWeek = startDate.getDayOfWeek();

            if (!isWeekEnd(dayOfWeek)) {
                BigDecimal currentPrice = stockTimeSeries.getFloorClose(startDate);

                BigDecimal currentExchangeRate = exchangeRatesTimeSeries.floorEntry(startDate).getValue()
                        .getRate(holdingEager.getCurrency());

                if (currentPrice != null && currentExchangeRate != null) {
                    HoldingPerformance holdingPerformance = calculateHoldingPerformance(holdingEager, currentPrice, currentExchangeRate, startDate);

                    holdingPerformanceTimeSeries.put(startDate, holdingPerformance);
                }
            }

            startDate = startDate.plusDays(1);
        }

        return holdingPerformanceTimeSeries;
    }

    /* convenience method */

    public TreeMap<LocalDate, HoldingPerformance> getHoldingPerformanceTimeSeries(Holding holdingEager) {
        LocalDate firstTradeDate = holdingEager.getFirstTradeDate();
        return getHoldingPerformanceTimeSeriesOptimized(holdingEager, firstTradeDate);
    }

    /* Portfolio performance */

    private PortfolioPerformance calculatePortfolioPerformance(Portfolio portfolioEager, List<Holding> holdings,
                                                               Map<String, BigDecimal> priceMap,
                                                               ExchangeRates exchangeRates, LocalDate deadLine) {

        BigDecimal currentValue = BigDecimal.ZERO; // number of shares * current price
        BigDecimal totalSales = BigDecimal.ZERO; // all sales
        BigDecimal totalDividends = BigDecimal.ZERO; // total amount of dividends
        BigDecimal totalCosts = BigDecimal.ZERO; // all buys with brokerage + brokerage of sales

        List<HoldingPerformance> holdingPerformances = new ArrayList<>();

        for (Holding currentHolding : holdings) {

            BigDecimal currentPrice = priceMap.get(currentHolding.getFullyQualifiedSymbol());// .getFloorClose(deadLine);

            BigDecimal currentExchangeRate = exchangeRates.getRate(currentHolding.getCurrency());

            HoldingPerformance holdingPerformance = calculateHoldingPerformance(currentHolding, currentPrice, currentExchangeRate, deadLine);

            totalSales = totalSales.add(holdingPerformance.getTotalSales());
            totalCosts = totalCosts.add(holdingPerformance.getTotalCosts());
            currentValue = currentValue.add(holdingPerformance.getCurrentValue());
            totalDividends = totalDividends.add(holdingPerformance.getTotalDividends());
            holdingPerformances.add(holdingPerformance);
        }

        BigDecimal totalEarnings = currentValue.add(totalSales).add(totalDividends);

        BigDecimal totalReturn = totalEarnings.subtract(totalCosts);

        BigDecimal totalPercentReturn = totalReturn.divide(totalCosts, SCALE, ROUNDING_MODE);

        return new PortfolioPerformance(portfolioEager, holdingPerformances, deadLine, currentValue, totalSales, totalDividends,
                totalEarnings, totalCosts, totalReturn, totalPercentReturn);

    }

    private PortfolioPerformance getPortfolioPerformanceOptimized(Portfolio portfolioEager,
                                                                  Function<Portfolio, List<Holding>> holdingsExtractor,
                                                                  LocalDate deadLine) {

        List<Holding> holdings = portfolioEager == null ? null : holdingsExtractor.apply(portfolioEager);

        // last condition only for overall performance time series
        if (holdings == null || holdings.isEmpty() || getFirstTradeDate(holdings).compareTo(deadLine) > 0) {
            return new PortfolioPerformance(portfolioEager);
        }

        List<String> fullyQualifiedSymbols = getFullyQualifiedSymbols(holdings);

        Map<String, StockTimeSeries> stockTimeSeriesMap = alphaVantageService.getTimeSeriesDaily(fullyQualifiedSymbols);

        Map<String, BigDecimal> priceMap = getFloorCloseMap(deadLine, stockTimeSeriesMap);

        Currency portfolioCurrency = portfolioEager.getCurrency();

        ExchangeRates exchangeRates = exchangeRatesBean.getFloorEntry(deadLine, portfolioCurrency);

        return calculatePortfolioPerformance(portfolioEager, holdings, priceMap, exchangeRates, deadLine);
    }


//    private PortfolioPerformance getPortfolioPerformance(Portfolio portfolioEager,
//                                                         Function<Portfolio, List<Holding>> holdingsExtractor,
//                                                         LocalDate deadLine) {
//
//        List<Holding> holdings = portfolioEager == null ? null : holdingsExtractor.apply(portfolioEager);
//
//        if (holdings == null || holdings.isEmpty() || getFirstTradeDate(holdings).compareTo(deadLine) > 0) {
//            return new PortfolioPerformance(portfolioEager);
//        }
//
//        List<String> symbols = portfolioEager.getFullyQualifiedSymbols();
//
//        Map<String, StockTimeSeries> symbolToTimeSeries = alphaVantageService.getTimeSeriesDaily(symbols);
//
//        Currency portfolioCurrency = portfolioEager.getCurrency();
//
//        ExchangeRates exchangeRates = exchangeRatesBean.getFloorEntry(deadLine, portfolioCurrency);
//
//        BigDecimal currentValue = BigDecimal.ZERO; // number of shares * current price
//        BigDecimal totalSales = BigDecimal.ZERO; // all sales
//        BigDecimal totalDividends = BigDecimal.ZERO; // total amount of dividends
//        BigDecimal totalCosts = BigDecimal.ZERO; // all buys with brokerage + brokerage of sales
//
//        List<HoldingPerformance> holdingPerformances = new ArrayList<>();
//
//        for (Holding currentHolding : holdings) {
////            if(currentHolding.getFirstTradeDate().compareTo(deadLine) > 0){
////                continue;
////            }
//
//            BigDecimal currentPrice = symbolToTimeSeries.get(currentHolding.getFullyQualifiedSymbol()).getFloorClose(deadLine);
//
//            BigDecimal currentExchangeRate = exchangeRates.getRate(currentHolding.getCurrency());
//
//            HoldingPerformance holdingPerformance = calculateHoldingPerformance(currentHolding, currentPrice, currentExchangeRate, deadLine);
//
//            totalSales = totalSales.add(holdingPerformance.getTotalSales());
//            totalCosts = totalCosts.add(holdingPerformance.getTotalCosts());
//            currentValue = currentValue.add(holdingPerformance.getCurrentValue());
//            totalDividends = totalDividends.add(holdingPerformance.getTotalDividends());
//            holdingPerformances.add(holdingPerformance);
//        }
//
//        BigDecimal totalEarnings = currentValue.add(totalSales).add(totalDividends);
//
//        BigDecimal totalReturn = totalEarnings.subtract(totalCosts);
//
//        BigDecimal totalPercentReturn = totalReturn.divide(totalCosts, SCALE, ROUNDING_MODE);
//
//        return new PortfolioPerformance(portfolioEager, holdingPerformances, deadLine, currentValue, totalSales, totalDividends,
//                totalEarnings, totalCosts, totalReturn, totalPercentReturn);
//    }

    /* convenience methods */

    public PortfolioPerformance getPortfolioPerformance(Portfolio portfolioEager, boolean withClosed) {
        if (withClosed) {
            return getPortfolioPerformance(portfolioEager);
        }

        return getPortfolioPerformanceNotClosed(portfolioEager);
    }

    private PortfolioPerformance getPortfolioPerformance(Portfolio portfolioEager) {

        return getPortfolioPerformanceOptimized(portfolioEager, Portfolio::getHoldings, LocalDate.now());
    }


    private PortfolioPerformance getPortfolioPerformanceNotClosed(Portfolio portfolioEager) {

        return getPortfolioPerformanceOptimized(portfolioEager, Portfolio::getHoldingsNotClosed, LocalDate.now());
    }

    public List<PortfolioPerformance> getPortfolioPerformances(List<Portfolio> portfolios, boolean withClosed) {
        if (portfolios == null || portfolios.isEmpty())
            return new ArrayList<>();

        return portfolios.stream()
                .map(portfolio -> getPortfolioPerformance(portfolio, withClosed))
                .collect(Collectors.toList());
    }

    /* portfolio performance time series */

    private TreeMap<LocalDate, PortfolioPerformance> getPortfolioPerformanceTimeSeriesOptimized(Portfolio portfolioWithHoldings,
                                                                                                Function<Portfolio, List<Holding>> holdingsExtractor) {
        TreeMap<LocalDate, PortfolioPerformance> portfolioPerformanceTimeSeries = new TreeMap<>();

        List<Holding> holdings = portfolioWithHoldings == null ? null : holdingsExtractor.apply(portfolioWithHoldings);

        if (holdings == null || holdings.isEmpty()) {
            return portfolioPerformanceTimeSeries;
        }

        LocalDate deadLine = getFirstTradeDate(holdings);

        List<String> symbols = getFullyQualifiedSymbols(holdings);

        TreeMap<String, StockTimeSeries> stockTimeSeriesMap = alphaVantageService.getTimeSeriesDaily(symbols);

        NavigableMap<LocalDate, ExchangeRates> exchangeRatesMap = exchangeRatesBean.getFloorTailMap(deadLine,
                portfolioWithHoldings.getCurrency());

        while (deadLine.compareTo(LocalDate.now()) <= 0) {
            DayOfWeek day = deadLine.getDayOfWeek();
            if (!isWeekEnd(day)) {
                Map<String, BigDecimal> priceMap = getFloorCloseMap(deadLine, stockTimeSeriesMap);

                ExchangeRates exchangeRates = exchangeRatesMap.floorEntry(deadLine).getValue();

                PortfolioPerformance portfolioPerformance = calculatePortfolioPerformance(portfolioWithHoldings, holdings, priceMap, exchangeRates, deadLine);

                portfolioPerformance.clearHoldingPerformances();

                portfolioPerformanceTimeSeries.put(deadLine, portfolioPerformance);
            }
            deadLine = deadLine.plusDays(1);
        }

        return portfolioPerformanceTimeSeries;
    }

//    private TreeMap<LocalDate, PortfolioPerformance> getPortfolioPerformanceTimeSeries(Portfolio portfolioWithHoldings,
//                                                                                       Function<Portfolio, List<Holding>> holdingsExtractor) {
//        TreeMap<LocalDate, PortfolioPerformance> portfolioPerformanceTimeSeries = new TreeMap<>();
//
//        List<Holding> holdings = portfolioWithHoldings == null ? null : holdingsExtractor.apply(portfolioWithHoldings);
//
//        if (holdings == null || holdings.isEmpty()) {
//            return portfolioPerformanceTimeSeries;
//        }
//
//        LocalDate deadLine = getFirstTradeDate(holdings);
//
//        while (deadLine.compareTo(LocalDate.now()) <= 0) {
//            DayOfWeek day = deadLine.getDayOfWeek();
//            if (!isWeekEnd(day)) {
//                PortfolioPerformance portfolioPerformance = getPortfolioPerformance(portfolioWithHoldings, holdingsExtractor, deadLine);
//                portfolioPerformance.clearHoldingPerformances();
//                portfolioPerformanceTimeSeries.put(deadLine, portfolioPerformance);
//            }
//            deadLine = deadLine.plusDays(1);
//        }
//
//        return portfolioPerformanceTimeSeries;
//    }


    /* convenience methods */

    public TreeMap<LocalDate, PortfolioPerformance> getPortfolioPerformanceTimeSeries(Portfolio portfolioEager, boolean withClosed) {
        return withClosed ? getPortfolioPerformanceTimeSeries(portfolioEager) :
                getPortfolioPerformanceTimeSeriesNoClosed(portfolioEager);
    }


    private TreeMap<LocalDate, PortfolioPerformance> getPortfolioPerformanceTimeSeries(Portfolio portfolioEager) {

        return getPortfolioPerformanceTimeSeriesOptimized(portfolioEager, Portfolio::getHoldings);
    }

    private TreeMap<LocalDate, PortfolioPerformance> getPortfolioPerformanceTimeSeriesNoClosed(Portfolio portfolioEager) {

        return getPortfolioPerformanceTimeSeriesOptimized(portfolioEager, Portfolio::getHoldingsNotClosed);
    }

    /* overall performance */

    public OverallPerformance getOverallPerformance(List<Portfolio> portfolios, boolean withClosed) {
        return getOverallPerformance(portfolios, LocalDate.now(), withClosed);
    }

    public OverallPerformance getOverallPerformance(List<Portfolio> portfolios) {
        return getOverallPerformance(portfolios, LocalDate.now(), true);
    }

    public OverallPerformance getOverallPerformanceNotClosed(List<Portfolio> portfolios) {
        return getOverallPerformance(portfolios, LocalDate.now(), false);
    }


    private OverallPerformance getOverallPerformance(List<Portfolio> portfolios, LocalDate deadLine,
                                                     boolean withClosed) {
        if (portfolios == null || portfolios.isEmpty())
            return new OverallPerformance();

        List<Portfolio> portfoliosWithHoldings = portfolios.stream()
                .filter(Portfolio::hasHoldings)
                .collect(Collectors.toList());

        if (portfoliosWithHoldings.isEmpty()) {
            List<PortfolioPerformance> portfolioPerformances = new ArrayList<>();

            for (Portfolio portfolio : portfolios) {
                portfolioPerformances.add(new PortfolioPerformance(portfolio));
            }
            return new OverallPerformance(portfolioPerformances);
        }

        BigDecimal currentValue = BigDecimal.ZERO; // number of shares * current price
        BigDecimal totalSales = BigDecimal.ZERO; // all sales
        BigDecimal totalDividends = BigDecimal.ZERO; // total amount of dividends
        BigDecimal totalCosts = BigDecimal.ZERO; // all buys with brokerage + brokerage of sales

        List<PortfolioPerformance> portfolioPerformances = new ArrayList<>();
        for (Portfolio portfolio : portfoliosWithHoldings) {

            PortfolioPerformance portfolioPerformance = getPortfolioPerformance(portfolio, withClosed);

            portfolioPerformance.clearHoldingPerformances();

            portfolioPerformances.add(portfolioPerformance);

            currentValue = currentValue.add(portfolioPerformance.getCurrentValue());

            totalSales = totalSales.add(portfolioPerformance.getTotalSales());

            totalDividends = totalDividends.add(portfolioPerformance.getTotalDividends());

            totalCosts = totalCosts.add(portfolioPerformance.getTotalCosts());
        }

        BigDecimal totalEarnings = currentValue.add(totalSales).add(totalDividends);
        BigDecimal totalReturn = totalEarnings.subtract(totalCosts);
        BigDecimal totalPercentReturn = totalReturn.divide(totalCosts, SCALE, ROUNDING_MODE);

        return new OverallPerformance(portfolioPerformances, deadLine, currentValue, totalSales, totalDividends,
                totalEarnings, totalCosts, totalReturn, totalPercentReturn);
    }

    /* overall performance time series */

    private TreeMap<LocalDate, OverallPerformance> getOverallPerformanceTimeSeries(List<Portfolio> portfolios,
                                                                                   boolean withClosed) {
        TreeMap<LocalDate, OverallPerformance> overallPerformanceTimeSeries = new TreeMap<>();

        if (portfolios == null || portfolios.isEmpty()) {
            return overallPerformanceTimeSeries;
        }

        List<Portfolio> portfoliosWithHoldings = portfolios.stream()
                .filter(Portfolio::hasHoldings)
                .collect(Collectors.toList());

        LocalDate deadLine = portfoliosWithHoldings.stream()
                .map(Portfolio::getFirstTradeDate)
                .min(Comparator.naturalOrder()).orElseThrow(IllegalStateException::new);

        while (deadLine.compareTo(LocalDate.now()) <= 0) {
            DayOfWeek day = deadLine.getDayOfWeek();
            if (!isWeekEnd(day)) {
                OverallPerformance overallPerformance = getOverallPerformance(portfoliosWithHoldings, deadLine, withClosed);
                overallPerformance.clearPortfolioPerformances();
                overallPerformanceTimeSeries.put(deadLine, overallPerformance);
            }
            deadLine = deadLine.plusDays(1);
        }

        return overallPerformanceTimeSeries;
    }

    /* monthly financial performance time series */

    public TreeMap<LocalDate, IntervalPerformance> getMonthlyPerformanceTimeSeries
            (TreeMap<LocalDate, ? extends FinancialPerformance> financialPerformanceTimeSeries) {

        TreeMap<LocalDate, IntervalPerformance> resultMap = new TreeMap<>();

        if (financialPerformanceTimeSeries == null || financialPerformanceTimeSeries.isEmpty()) {
            return resultMap;
        }

        LocalDate[] sortedDates = new LocalDate[financialPerformanceTimeSeries.size()];

        financialPerformanceTimeSeries.keySet().toArray(sortedDates);

        int i = 0;
        BigDecimal open = financialPerformanceTimeSeries.firstEntry().getValue().getTotalReturn();
        while (i < sortedDates.length) { // loop through months

            int j = i + 1;

            while (j < sortedDates.length) { // loop through days of month
                if (sortedDates[i].getMonth() != sortedDates[j].getMonth()) {
                    break;
                }
                j++;
            }

            LocalDate firstOfMonth = sortedDates[i];

            LocalDate lastOfMonth = sortedDates[j - 1];

            BigDecimal close = financialPerformanceTimeSeries.get(lastOfMonth).getTotalReturn();

            resultMap.put(lastOfMonth, new IntervalPerformance(firstOfMonth, lastOfMonth, open, close));

            open = close; // set open to the previous close

            i = j; // increase outer month loop
        }

        return resultMap;
    }

    /* interval financial time series */

    public TreeMap<LocalDate, IntervalPerformance> getIntervalPerformanceTimeSeries(TreeMap<LocalDate,
            ? extends FinancialPerformance> financialPerformanceTimeSeries) {

        TreeMap<LocalDate, IntervalPerformance> resultMap = new TreeMap<>();

        if (financialPerformanceTimeSeries == null || financialPerformanceTimeSeries.isEmpty()) {
            return resultMap;
        }

        int numberOfDates = financialPerformanceTimeSeries.size();

        LocalDate[] sortedDates = new LocalDate[numberOfDates];

        financialPerformanceTimeSeries.keySet().toArray(sortedDates);

        int numberOfSteps = 4;

        int stepSize = (int) Math.ceil((1.0 * numberOfDates) / numberOfSteps);

        FinancialPerformance startingPoint = financialPerformanceTimeSeries.get(sortedDates[0]);

        for (int i = stepSize; i < numberOfDates; i += stepSize) {

            int currentIndex = i;

            if (i + stepSize >= numberOfDates) {
                currentIndex = numberOfDates - 1;
            }

            FinancialPerformance endPoint = financialPerformanceTimeSeries.get(sortedDates[currentIndex]);
            LocalDate startDate = sortedDates[i - stepSize + 1];
            LocalDate endDate = sortedDates[currentIndex];

            IntervalPerformance intervalPerformance =
                    new IntervalPerformance(startDate, endDate, startingPoint.getTotalReturn(), endPoint.getTotalReturn());

            resultMap.put(endDate, intervalPerformance);

            startingPoint = endPoint;
        }

        return resultMap;
    }

    /* small helpers */

    private List<String> getFullyQualifiedSymbols(List<Holding> holdings) {
        return holdings.stream()
                .map(Holding::getFullyQualifiedSymbol)
                .collect(Collectors.toList());
    }

    private LocalDate getFirstTradeDate(List<Holding> holdings) {
        return holdings.stream()
                .flatMap(holding -> holding.getTrades().stream())
                .map(Trade::getDate)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalArgumentException("Holding without trades exist!"));
    }

    private Map<String, BigDecimal> getFloorCloseMap(LocalDate deadLine, Map<String, StockTimeSeries> symbolToTimeSeries) {
        Map<String, BigDecimal> symbolToPrice = new HashMap<>();
        for (StockTimeSeries stockTimeSeries : symbolToTimeSeries.values()) {

            BigDecimal currentPrice = stockTimeSeries.getFloorClose(deadLine);

            symbolToPrice.put(stockTimeSeries.getFullyQualifiedSymbol(), currentPrice);
        }

        return symbolToPrice;
    }

    private static boolean isWeekEnd(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}

