package at.uibk.apis.stocks.worldTradingData.parser;

import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.apis.stocks.worldTradingData.responseDataTypes.StockDataTimeSeries;
import at.uibk.apis.stocks.worldTradingData.responseDataTypes.StockDataTimeSeriesEntry;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeMap;

public class StockDataTimeSeriesParser extends WorldTradingDataBaseParser<StockDataTimeSeries> {

    @Override
    protected StockDataTimeSeries resolve(JSONObject jsonRootObject) {
        try {
            String fullyQualifiedSymbol = jsonRootObject.getString("name");

            JSONObject jsonTimeSeries = jsonRootObject.getJSONObject("history");

            TreeMap<LocalDate, StockDataTimeSeriesEntry> timeSeriesEntries = getTimeSeriesEntries(jsonTimeSeries);

            return new StockDataTimeSeries(fullyQualifiedSymbol, timeSeriesEntries);

        } catch (JSONException e) {
            throw new AlphaVantageException("General result format may have changed!");
        }
    }

    private TreeMap<LocalDate, StockDataTimeSeriesEntry> getTimeSeriesEntries(JSONObject jsonTimeSeries) {
        TreeMap<LocalDate, StockDataTimeSeriesEntry> timeSeriesEntries = new TreeMap<>();

        for (String key : jsonTimeSeries.keySet()) {
            JSONObject jsonTimeSeriesObject = jsonTimeSeries.getJSONObject(key);

            LocalDate date = ParserUtils.parseSimpleDate(key);
            BigDecimal open = jsonTimeSeriesObject.getBigDecimal("open");
            BigDecimal high = jsonTimeSeriesObject.getBigDecimal("high");
            BigDecimal low = jsonTimeSeriesObject.getBigDecimal("low");
            BigDecimal close = jsonTimeSeriesObject.getBigDecimal("close");
            long volume = jsonTimeSeriesObject.getLong("volume");

            StockDataTimeSeriesEntry stockDataTimeSeriesEntry = new StockDataTimeSeriesEntry(date, open, high, low, close, volume);

            timeSeriesEntries.put(date, stockDataTimeSeriesEntry);
        }

        return timeSeriesEntries;
    }


}
