package at.uibk.financial_evaluation;

import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeries;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeriesEntry;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.TreeMap;

public class StockPerformanceEvaluator implements Serializable {
    private static final int[] MONTHS_BACK_LIST = new int[]{1,3,6,12,24,36};

    private TreeMap<LocalDate, StockTimeSeriesEntry> timeSeries;

    public StockPerformanceEvaluator() {
    }

    public StockPerformanceEvaluator(StockTimeSeries stockTimeSeries) {
        setTimeSeries(stockTimeSeries);
    }

    public void setTimeSeries(StockTimeSeries stockTimeSeries) {
        if(stockTimeSeries == null) {
            timeSeries = null;
            return;
        }

        timeSeries = stockTimeSeries.getEntries();
    }

    private StockPerformanceEntry calculatePerformance(int monthsBack) {
        if(timeSeries == null || timeSeries.isEmpty())
            return new StockPerformanceEntry();

        LocalDate deadLine = timeSeries.lastKey().minusMonths(monthsBack);

        NavigableMap<LocalDate, StockTimeSeriesEntry> currentEntries = this.timeSeries
                .tailMap(deadLine, true);

        StockTimeSeriesEntry firstEntry = currentEntries.firstEntry().getValue();
        StockTimeSeriesEntry lastEntry = currentEntries.lastEntry().getValue();

        BigDecimal open = firstEntry.getClose();
        BigDecimal close = lastEntry.getClose();
        BigDecimal low = firstEntry.getOpen();
        BigDecimal high = firstEntry.getOpen();
        long volume = 0;

        for (StockTimeSeriesEntry currentEntry : currentEntries.values()) {
            if(high.compareTo(currentEntry.getHigh()) < 0){
                high = currentEntry.getHigh();
            }

            if(low.compareTo(currentEntry.getLow()) > 0){
                low = currentEntry.getLow();
            }

            volume += currentEntry.getVolume();
        }

        LocalDate fromDate = firstEntry.getDate();
        LocalDate toDate = lastEntry.getDate();

        return new StockPerformanceEntry(monthsBack, fromDate, toDate, open, high, low, close, volume);
    }

    public NavigableMap<Integer, StockPerformanceEntry> getPerformanceMap(){
        NavigableMap<Integer, StockPerformanceEntry> performanceMap = new TreeMap<>();

        if(timeSeries == null || timeSeries.isEmpty()){
            return performanceMap;
        }

        LocalDate startDate = timeSeries.firstKey();
        LocalDate endDate = timeSeries.lastKey();

        for(int monthsBacks: MONTHS_BACK_LIST){
            LocalDate deadLine = endDate.minusMonths(monthsBacks);

            if(deadLine.compareTo(startDate) < 0)
                return performanceMap;

            performanceMap.put(monthsBacks, calculatePerformance(monthsBacks));
        }

        return performanceMap;
    }


}
