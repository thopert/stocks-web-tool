package at.uibk.apis.stocks.alphaVantage.tools;

import at.uibk.apis.stocks.alphaVantage.connector.AlphaVantageConnector;
import at.uibk.apis.stocks.alphaVantage.endpoints.AlphaVantageQuoteEndPoint;
import at.uibk.apis.stocks.alphaVantage.endpoints.AlphaVantageSearchEndPoint;
import at.uibk.apis.stocks.alphaVantage.endpoints.AlphaVantageTimeSeriesEndPoint;
import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.apis.stocks.alphaVantage.parameter.OutputSize;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockExchange;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockQuote;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockSearchResult;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeries;
import at.uibk.model.mainEntities.StockIdentifier;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;

public class AlphaVantageEndPointsTool implements Serializable {
    private AlphaVantageQuoteEndPoint quoteEndPoint;
    private AlphaVantageTimeSeriesEndPoint timeSeriesEndPoint;
    private AlphaVantageSearchEndPoint searchEndPoint;

    public AlphaVantageEndPointsTool(AlphaVantageConnector connector) {
        this.quoteEndPoint = new AlphaVantageQuoteEndPoint(connector);
        this.timeSeriesEndPoint = new AlphaVantageTimeSeriesEndPoint(connector);
        this.searchEndPoint = new AlphaVantageSearchEndPoint(connector);
    }

    /* search */

    public List<StockSearchResult> getMatches(String keywords) {
        return searchEndPoint.getMatches(keywords);
    }

    public Optional<StockSearchResult> searchSymbol(String symbol, StockExchange exchange) {
        String searchSymbol = symbol.toUpperCase();
        String searchExpression = symbol + exchange.getSearchExtension();

        List<StockSearchResult> matches = searchEndPoint.getMatches(searchExpression);

        Predicate<StockSearchResult> isRequestedSymbol =
                ssr -> ssr.getFullyQualifiedSymbol().equalsIgnoreCase(searchSymbol + exchange.getExtension());

        return matches.stream()
                .filter(isRequestedSymbol)
                .findFirst();
    }

    public Optional<StockSearchResult> searchSymbol(StockIdentifier stockIdentifier) {
        List<StockSearchResult> matches = searchEndPoint.getMatches(stockIdentifier.getSearchExpression());

        Predicate<StockSearchResult> isRequestedSymbol =
                ssr -> ssr.getFullyQualifiedSymbol().equalsIgnoreCase(stockIdentifier.getFullyQualifiedSymbol());

        return matches.stream()
                .filter(isRequestedSymbol)
                .findFirst();
    }

    /* quotes */

    public StockQuote getQuote(String fullyQualifiedSymbol) {
        return quoteEndPoint.getData(fullyQualifiedSymbol);
    }

    public TreeMap<String, StockQuote> getQuote(Collection<String> fullyQualifiedSymbols) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<StockQuote>> futures = new ArrayList<>();
        TreeMap<String, StockQuote> resultMap = new TreeMap<>();

        for (String fullyQualifiedSymbol : fullyQualifiedSymbols) {
            futures.add(executorService.submit(() -> quoteEndPoint.getData(fullyQualifiedSymbol)));
        }

        executorService.shutdown();

        for (Future<StockQuote> future : futures) {
            StockQuote stockQuote;
            try {
                stockQuote = future.get();
                resultMap.put(stockQuote.getFullyQualifiedSymbol(), stockQuote);
            } catch (Exception e) {
                throw new AlphaVantageException(e.getMessage());
            }
        }

        return resultMap;
    }

    /* time series */

    public StockTimeSeries getTimeSeriesDaily(StockIdentifier stockIdentifier, OutputSize outputSize) {
        return timeSeriesEndPoint.getDays(stockIdentifier.getFullyQualifiedSymbol(), outputSize);
    }

    public StockTimeSeries getTimeSeriesDaily(String fullyQualifiedSymbol, OutputSize outputSize) {
        return timeSeriesEndPoint.getDays(fullyQualifiedSymbol, outputSize);
    }

//    public TreeMap<String, StockTimeSeries> getTimeSeriesDaily(Collection<StockIdentifier> stockIdentifiers) {
//        List<String> fullyQualifiedSymbols = stockIdentifiers.stream()
//                .map(StockIdentifier::getFullyQualifiedSymbol)
//                .collect(Collectors.toList());
//        return getTimeSeriesDaily(fullyQualifiedSymbols);
//    }

    public TreeMap<String, StockTimeSeries> getTimeSeriesDaily(Collection<String> fullyQualifiedSymbols) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<StockTimeSeries>> futures = new ArrayList<>();
        TreeMap<String, StockTimeSeries> resultMap = new TreeMap<>();

        for (String fullyQualifiedSymbol : fullyQualifiedSymbols) {
            futures.add(executorService.submit(() -> timeSeriesEndPoint.getDays(fullyQualifiedSymbol, OutputSize.FULL)));
        }

        executorService.shutdown();

        for (Future<StockTimeSeries> future : futures) {
            StockTimeSeries stockQuote;
            try {
                stockQuote = future.get();
                resultMap.put(stockQuote.getFullyQualifiedSymbol(), stockQuote);
            } catch (Exception e) {
                throw new AlphaVantageException(e.getMessage());
            }
        }

        return resultMap;
    }
}