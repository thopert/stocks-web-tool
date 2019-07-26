package at.uibk.charts.specificCharts;

import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeriesEntry;
import at.uibk.charts.annotations.SingleStockChart;
import at.uibk.charts.basicCharts.SimpleTimeSeriesLineChart;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@SingleStockChart
public class SingleStockTimeSeriesChart extends SimpleTimeSeriesLineChart<StockTimeSeriesEntry> implements Serializable {
    private static final String DEFAULT_EXTRACTOR_TYPE = "close";
    private static final int DEFAULT_MONTHS_BACK = 1;

    private String extractorType = DEFAULT_EXTRACTOR_TYPE;

    public SingleStockTimeSeriesChart() {
        setExtractorType(DEFAULT_EXTRACTOR_TYPE);
        setMonthsBack(DEFAULT_MONTHS_BACK);
    }

    public void setExtractorType(String extractorType) {
        switch (extractorType) {
            case "open":
                setDataPointExtractor(StockTimeSeriesEntry::getOpen);
                break;
            case "high":
                setDataPointExtractor(StockTimeSeriesEntry::getHigh);
                break;
            case "low":
                setDataPointExtractor(StockTimeSeriesEntry::getLow);
                break;
            case "close":
                setDataPointExtractor(StockTimeSeriesEntry::getClose);
                break;
            default:
                throw new IllegalArgumentException("Wrong extractor type!");
        }

        this.extractorType = extractorType;
    }

    public String getExtractorType() {
        return extractorType;
    }

    public List<String> getExtractorTypes() {
        return Arrays.asList("open", "high", "low", "close");
    }

    @Override
    public void applyDefaultSettings() {
        super.applyDefaultSettings();
        setMonthsBack(DEFAULT_MONTHS_BACK);
    }
}
