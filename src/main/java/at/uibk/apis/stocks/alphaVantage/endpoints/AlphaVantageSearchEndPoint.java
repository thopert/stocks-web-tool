package at.uibk.apis.stocks.alphaVantage.endpoints;

import at.uibk.apis.stocks.alphaVantage.connector.AlphaVantageConnector;
import at.uibk.apis.stocks.alphaVantage.parameter.Function;
import at.uibk.apis.stocks.alphaVantage.parameter.Keywords;
import at.uibk.apis.stocks.alphaVantage.parsing.SearchResponseParser;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockSearchResult;

import java.io.Serializable;
import java.util.List;

public class AlphaVantageSearchEndPoint implements Serializable {

    private AlphaVantageConnector connector;

    private SearchResponseParser searchResponseParser = new SearchResponseParser();

    public AlphaVantageSearchEndPoint(AlphaVantageConnector connector){
        this.connector = connector;
    }

    public List<StockSearchResult> getMatches(String keywords){
        String json = connector.doRequest(Function.SEARCH_ENDPOINT, new Keywords(keywords));
        return searchResponseParser.parse(json);
    }
}
