package at.uibk.apis.stocks.alphaVantage.parsing;

import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeries;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeriesEntry;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeMap;

public class StockTimeSeriesParser extends StockTimeSeriesBaseParser<StockTimeSeries> {

    private StockTimeSeriesParserType timeSeriesType;

    public StockTimeSeriesParser() {
    }

    public StockTimeSeriesParser(StockTimeSeriesParserType type) {
        this.timeSeriesType = type;
    }

    public void setTimeSeriesType(StockTimeSeriesParserType timeSeriesType) {
        this.timeSeriesType = timeSeriesType;
    }

    @Override
    protected String getTimeSeriesType() {
        return timeSeriesType.getKey();
    }

    @Override
    protected StockTimeSeries resolve(JSONObject metaData, JSONObject timeSeries) {
        try {
            String fullyQualifiedSymbol = metaData.getString("2. Symbol");

            // last refreshed may be date or timestamp depending on day of week --> time will be ignored
            String lastRefreshed = metaData.getString("3. Last Refreshed").split(" ", 2)[0];

            LocalDate lastRefreshedDate = ParserUtils.parseSimpleDate(lastRefreshed);

            TreeMap<LocalDate, StockTimeSeriesEntry> timeSeriesEntries = getTimeSeriesEntries(timeSeries);

            return new StockTimeSeries(fullyQualifiedSymbol, lastRefreshedDate, timeSeriesEntries);
        } catch (JSONException e) {
            throw new AlphaVantageException("General result format may have changed!");
        } catch (NumberFormatException e) {
            throw new AlphaVantageException("Number format changed!");
        }
    }

    private TreeMap<LocalDate, StockTimeSeriesEntry> getTimeSeriesEntries(JSONObject timeSeries) {
        TreeMap<LocalDate, StockTimeSeriesEntry> timeSeriesMap = new TreeMap<>();

        for (String key : timeSeries.keySet()) {
            JSONObject jsonTimeSeriesObject = timeSeries.getJSONObject(key);

            LocalDate date = ParserUtils.parseSimpleDate(key);
            BigDecimal open = jsonTimeSeriesObject.getBigDecimal("1. open");
            BigDecimal high = jsonTimeSeriesObject.getBigDecimal("2. high");
            BigDecimal low = jsonTimeSeriesObject.getBigDecimal("3. low");
            BigDecimal close = jsonTimeSeriesObject.getBigDecimal("4. close");
            BigDecimal adjustedClose = jsonTimeSeriesObject.getBigDecimal("5. adjusted close");
            long volume = jsonTimeSeriesObject.getLong("6. volume");
            BigDecimal dividendAmount = jsonTimeSeriesObject.getBigDecimal("7. dividend amount");
            BigDecimal splitCoefficient = jsonTimeSeriesObject.getBigDecimal("8. split coefficient");

//            if (close.compareTo(BigDecimal.ZERO) > 0)
            timeSeriesMap.put(date, new StockTimeSeriesEntry(date, open, high, low, close, adjustedClose,
                    dividendAmount, splitCoefficient, volume));
        }

        return timeSeriesMap;
    }
}
