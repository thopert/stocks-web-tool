package at.uibk.apis.stocks.alphaVantage.endpoints;

import at.uibk.apis.stocks.alphaVantage.connector.AlphaVantageConnector;
import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.apis.stocks.alphaVantage.parameter.Function;
import at.uibk.apis.stocks.alphaVantage.parameter.Keywords;
import at.uibk.apis.stocks.alphaVantage.parameter.OutputSize;
import at.uibk.apis.stocks.alphaVantage.parameter.Symbol;
import at.uibk.apis.stocks.alphaVantage.parsing.SearchResponseParser;
import at.uibk.apis.stocks.alphaVantage.parsing.StockQuoteParser;
import at.uibk.apis.stocks.alphaVantage.parsing.StockTimeSeriesParser;
import at.uibk.apis.stocks.alphaVantage.parsing.StockTimeSeriesParserType;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockQuote;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockSearchResult;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AlphaVantageEndPoints implements Serializable {

    private AlphaVantageConnector connector;
    private StockQuoteParser stockQuoteParser = new StockQuoteParser();
    private SearchResponseParser searchResponseParser = new SearchResponseParser();
    private StockTimeSeriesParser stockTimeSeriesParser = new StockTimeSeriesParser();

    public AlphaVantageEndPoints(AlphaVantageConnector connector) {
        this.connector = connector;
    }

    public List<StockSearchResult> getMatches(String keywords) {
        String json = connector.doRequest(Function.SEARCH_ENDPOINT, new Keywords(keywords));
        return searchResponseParser.parse(json);
    }

    public StockQuote getQuote(String fullyQualifiedSymbol) {
        String json = connector.doRequest(new Symbol(fullyQualifiedSymbol), Function.QUOTE_ENDPOINT);
        return this.stockQuoteParser.parse(json);
    }

    public TreeMap<String, StockQuote> getQuote(Collection<String> symbols) {
        ExecutorService executorService = Executors.newFixedThreadPool(symbols.size());

        List<Future<StockQuote>> futures = new ArrayList<>();

        for (String symbol : symbols) {
            futures.add(executorService.submit(() ->
                    stockQuoteParser.parse(connector.doRequest(new Symbol(symbol), Function.QUOTE_ENDPOINT))));
        }

        executorService.shutdown();

        TreeMap<String, StockQuote> resultMap = new TreeMap<>();

        for (Future<StockQuote> f : futures) {
            StockQuote stockQuote;
            try {
                stockQuote = f.get();
                resultMap.put(stockQuote.getFullyQualifiedSymbol(), stockQuote);
            } catch (Exception e) {
                throw new AlphaVantageException(e.getMessage());
            }
        }
        return resultMap;
    }

    public StockTimeSeries getTimeSeriesDaily(String symbol, OutputSize outputSize) {
        String json = connector.doRequest(new Symbol(symbol), Function.DAILY, outputSize);
        stockTimeSeriesParser.setTimeSeriesType(StockTimeSeriesParserType.DAILY);
        return stockTimeSeriesParser.parse(json);
    }
}
