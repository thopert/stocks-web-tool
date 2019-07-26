package at.uibk.apis.stocks.alphaVantage.endpoints;

import at.uibk.apis.stocks.alphaVantage.connector.AlphaVantageConnector;
import at.uibk.apis.stocks.alphaVantage.parameter.Function;
import at.uibk.apis.stocks.alphaVantage.parameter.Symbol;
import at.uibk.apis.stocks.alphaVantage.parsing.StockQuoteParser;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockQuote;

import java.io.Serializable;

public class AlphaVantageQuoteEndPoint implements Serializable {

	private AlphaVantageConnector connector;

	private StockQuoteParser stockQuoteParser = new StockQuoteParser();

	public AlphaVantageQuoteEndPoint(AlphaVantageConnector connector) {
		this.connector = connector;
	}

	public StockQuote getData(String fullyQualifiedSymbol) {
		String json = connector.doRequest(new Symbol(fullyQualifiedSymbol), Function.QUOTE_ENDPOINT);
		return this.stockQuoteParser.parse(json);
	}

}
