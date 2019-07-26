package at.uibk.services.apis;

import at.uibk.apis.stocks.alphaVantage.parameter.OutputSize;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockSearchResult;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeries;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeriesEntry;
import at.uibk.apis.stocks.alphaVantage.tools.AlphaVantageEndPointsTool;
import at.uibk.model.mainEntities.StockIdentifier;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SessionScoped
public class AlphaVantageService implements Serializable {

    @Inject
    private AlphaVantageEndPointsTool alphaVantageEndPointsTool;

    private LRUCache<String, StockTimeSeries> stockTimeSeriesCache = new LRUCache<>();

    private String normalizeSymbol(String symbol) {
        return symbol.trim().toUpperCase();
    }

    public List<StockSearchResult> getMatches(String keywords) {
        return alphaVantageEndPointsTool.getMatches(keywords);
    }

    public Optional<StockSearchResult> searchSymbol(StockIdentifier stockIdentifier) {
        return alphaVantageEndPointsTool.searchSymbol(stockIdentifier);
    }

    public BigDecimal getPrice(StockIdentifier stockIdentifier) {
        return getPrice(stockIdentifier.getFullyQualifiedSymbol());
    }

    public BigDecimal getPrice(String fullyQualifiedSymbol) {
        fullyQualifiedSymbol = normalizeSymbol(fullyQualifiedSymbol);

        StockTimeSeries stockTimeSeries = stockTimeSeriesCache.get(fullyQualifiedSymbol);

        if (stockTimeSeries != null)
            return stockTimeSeries.getClose();

        // fetch time-series instead of quote
        stockTimeSeries = alphaVantageEndPointsTool.getTimeSeriesDaily(fullyQualifiedSymbol, OutputSize.FULL);

        stockTimeSeriesCache.put(fullyQualifiedSymbol, stockTimeSeries);

        return stockTimeSeries.getClose();
    }

    public TreeMap<String, StockTimeSeries> getTimeSeriesDaily(Collection<String> fullyQualifiedSymbols) {
        fullyQualifiedSymbols = fullyQualifiedSymbols.stream()
                .map(symbol -> normalizeSymbol(symbol))
                .collect(Collectors.toList());

        List<String> symbolsNotInCache = new ArrayList<>();

        TreeMap<String, StockTimeSeries> symbolToTimeSeries = new TreeMap<>();

        for (String symbol : fullyQualifiedSymbols) {
            StockTimeSeries stockTimeSeries = stockTimeSeriesCache.get(symbol);

            if (stockTimeSeries == null) {
                symbolsNotInCache.add(symbol);
            } else {
                symbolToTimeSeries.put(symbol, stockTimeSeries);
            }
        }

        if (!symbolsNotInCache.isEmpty()) {
            Map<String, StockTimeSeries> freshDataMap = alphaVantageEndPointsTool.getTimeSeriesDaily(symbolsNotInCache);

            freshDataMap.forEach((symbol, stockTimeSeries) -> {
                stockTimeSeriesCache.put(symbol, stockTimeSeries);
                symbolToTimeSeries.put(symbol, stockTimeSeries);
            });
        }
        return symbolToTimeSeries;
    }

    public StockTimeSeries getTimeSeriesDaily(StockIdentifier stockIdentifier) {
        return getTimeSeriesDaily(stockIdentifier.getFullyQualifiedSymbol());
    }

    public StockTimeSeries getTimeSeriesDaily(String fullyQualifiedSymbol) {
        fullyQualifiedSymbol = normalizeSymbol(fullyQualifiedSymbol);

        StockTimeSeries stockTimeSeries = stockTimeSeriesCache.get(fullyQualifiedSymbol);

        if (stockTimeSeries == null) {
            stockTimeSeries = alphaVantageEndPointsTool.getTimeSeriesDaily(fullyQualifiedSymbol, OutputSize.FULL);
            stockTimeSeriesCache.put(stockTimeSeries.getFullyQualifiedSymbol(), stockTimeSeries);
        }

        return stockTimeSeries;
    }

    public StockTimeSeriesEntry getFloorClose(StockIdentifier stockIdentifier, LocalDate localDate) {
        return getFloorClose(stockIdentifier.getFullyQualifiedSymbol(), localDate);
    }

    public StockTimeSeriesEntry getFloorClose(String fullyQualifiedSymbol, LocalDate localDate) {
        fullyQualifiedSymbol = normalizeSymbol(fullyQualifiedSymbol);

        StockTimeSeries stockTimeSeries = stockTimeSeriesCache.get(fullyQualifiedSymbol);

        if (stockTimeSeries == null) {
            stockTimeSeries = alphaVantageEndPointsTool.getTimeSeriesDaily(fullyQualifiedSymbol, OutputSize.FULL);
            stockTimeSeriesCache.put(stockTimeSeries.getFullyQualifiedSymbol(), stockTimeSeries);
        }

        return stockTimeSeries.getFloorEntry(localDate);
    }

    public TreeMap<LocalDate, StockTimeSeriesEntry> getSplitTimeSeries(String fullyQualifiedSymbol) {
        return getSpecificTimeSeriesEntries(fullyQualifiedSymbol, StockTimeSeriesEntry::isAdjustment);
    }

    public TreeMap<LocalDate, StockTimeSeriesEntry> getDividendTimeSeries(String fullyQualifiedSymbol) {
      return getSpecificTimeSeriesEntries(fullyQualifiedSymbol, StockTimeSeriesEntry::hasDividend);
    }

    private TreeMap<LocalDate, StockTimeSeriesEntry> getSpecificTimeSeriesEntries(String fullyQualifiedSymbol,
                                                                                  Predicate<StockTimeSeriesEntry> stockTimeSeriesEntryPredicate){
        StockTimeSeries timeSeriesDaily = getTimeSeriesDaily(fullyQualifiedSymbol);

        TreeMap<LocalDate, StockTimeSeriesEntry> timeSeriesEntries = timeSeriesDaily.getEntries().values().stream()
                .filter(stockTimeSeriesEntryPredicate)
                .collect(TreeMap::new,
                        (treeMap,
                         stockTimeSeriesEntry) -> treeMap.put(stockTimeSeriesEntry.getDate(), stockTimeSeriesEntry),
                        TreeMap::putAll);
        return timeSeriesEntries;
    }
}
