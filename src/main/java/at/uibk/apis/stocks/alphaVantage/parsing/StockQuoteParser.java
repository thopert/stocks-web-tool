package at.uibk.apis.stocks.alphaVantage.parsing;

import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.apis.stocks.alphaVantage.exceptions.InvalidSymbolException;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockQuote;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;


public class StockQuoteParser extends AlphaVantageBaseParser<StockQuote> {
	private static final String QUOTE_KEY = "Global Quote";

	@Override
	protected StockQuote resolve(JSONObject rootObject) {
		JSONObject globalQuote = rootObject.getJSONObject(QUOTE_KEY);
		StockQuote stockData = new StockQuote();

		if (globalQuote.isEmpty()){ // empty object was returned -> invalid symbol
			throw new InvalidSymbolException("Invalid symbol!");
		}

		try {
			stockData.setFullyQualifiedSymbol(globalQuote.getString("01. symbol"));
			stockData.setOpen(new BigDecimal(globalQuote.getString("02. open")));
			stockData.setHigh(new BigDecimal(globalQuote.getString("03. high")));
			stockData.setLow(new BigDecimal(globalQuote.getString("04. low")));
			stockData.setPrice(new BigDecimal(globalQuote.getString("05. price")));
			stockData.setVolume(Long.parseLong(globalQuote.getString("06. volume")));
			stockData.setLatestTradingDay(ParserUtils.parseSimpleDate(globalQuote.getString("07. latest trading day")));
			stockData.setPreviousClose(new BigDecimal(globalQuote.getString("08. previous close")));
			stockData.setChange(new BigDecimal(globalQuote.getString("09. change")));
			stockData.setChangePercent(parsePercent(globalQuote.getString("10. change percent")));
			return stockData;
		} catch (JSONException e){
			throw new AlphaVantageException("General result format has changed!");
		} catch (NumberFormatException e) {
            throw new AlphaVantageException("Number format changed!");
        }

	}

	private BigDecimal parsePercent(String percent) {
		return new BigDecimal(percent.substring(0, percent.length()-1));
	}

	public static void main(String[] args) {
		System.out.println(new BigDecimal(110.1));
	}
}
