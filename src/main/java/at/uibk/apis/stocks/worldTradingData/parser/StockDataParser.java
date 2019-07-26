package at.uibk.apis.stocks.worldTradingData.parser;

import at.uibk.apis.stocks.worldTradingData.exceptions.WorldTradingDataException;
import at.uibk.apis.stocks.worldTradingData.responseDataTypes.StockData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class StockDataParser extends WorldTradingDataBaseParser<List<StockData>> {
	private static final String DATA_KEY = "data";

	@Override
	protected List<StockData> resolve(JSONObject jsonRootObject) {
		JSONArray data = jsonRootObject.getJSONArray(DATA_KEY);

		if(data.length() == 0){
			throw new WorldTradingDataException("Empty Data!");
		}

		if(!hasAllRequestedSymbols(jsonRootObject)){
			throw new WorldTradingDataException("Missing Data!");
		}

		List<StockData> stockDataList = new ArrayList<>();

		for (int i = 0; i < data.length(); i++) {
			JSONObject jsonStockData = data.getJSONObject(0);

			try {
				String fullyQualifiedSymbol = jsonStockData.getString("symbol");
				BigDecimal price = jsonStockData.getBigDecimal("price");
				BigDecimal open = jsonStockData.getBigDecimal("price_open");
				BigDecimal high = jsonStockData.getBigDecimal("day_high");
				BigDecimal low = jsonStockData.getBigDecimal("day_low");
				BigDecimal yearHigh = jsonStockData.getBigDecimal("52_week_high");
				BigDecimal yearLow = jsonStockData.getBigDecimal("52_week_low");
				BigDecimal dayChange = jsonStockData.getBigDecimal("day_change");
				BigDecimal changePercent = jsonStockData.getBigDecimal("change_pct");
				BigDecimal previousClose = jsonStockData.getBigDecimal("close_yesterday");
				long volume = jsonStockData.getLong("volume");
				LocalDateTime lastTradeTime = ParserUtils.parseDateTime(jsonStockData.getString("last_trade_time"));

				StockData stockData = new StockData(fullyQualifiedSymbol, price, open, high, low, yearHigh, yearLow, lastTradeTime,
						dayChange, changePercent, previousClose, volume);

				stockDataList.add(stockData);

			} catch (JSONException e){
				throw new WorldTradingDataException("General result format has changed!");
			}
		}

		return stockDataList;
	}

	private boolean hasAllRequestedSymbols(JSONObject rootObject) {
		int numberOfRequested = rootObject.getInt("symbols_requested");
		int numberOfReturned = rootObject.getInt("symbols_returned");

		return numberOfRequested == numberOfReturned;
	}

}
