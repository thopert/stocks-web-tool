package at.uibk.charts.complexChart.chartEntries;

import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeries;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeriesEntry;
import at.uibk.charts.utils.StockIndex;
import at.uibk.model.mainEntities.Instrument;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class StockChartEntry extends ChartEntry<StockTimeSeriesEntry> {
    public static final List<String> VALUE_TYPES = Arrays.asList("open", "high", "low", "close");
    public static final String DEFAULT_VALUE_TYPE = "close";

    /* application logic */

    public StockChartEntry() {
    }

    public StockChartEntry(Instrument instrument, StockTimeSeries stockTimeSeries) {
        setId(instrument);
        setTimeSeries(stockTimeSeries.getEntries());
    }

    @Override
    protected BigDecimal extractValue(StockTimeSeriesEntry stockTimeSeriesEntry, String valueType) {
        if (stockTimeSeriesEntry == null)
            return null;

        if (valueType.equals("high"))
            return normalize(stockTimeSeriesEntry.getHigh());

        if (valueType.equals("low"))
            return normalize(stockTimeSeriesEntry.getLow());

        if (valueType.equals("open"))
            return normalize(stockTimeSeriesEntry.getOpen());

        return normalize(stockTimeSeriesEntry.getClose());
    }

    /* getter and setter */
    public void setId(Instrument instrument) {
        setId(instrument.getStockIdentifier().toString());
    }

    public void setId(StockIndex stockIndex) {
        setId(stockIndex.getFullyQualifiedSymbol());
    }
}

