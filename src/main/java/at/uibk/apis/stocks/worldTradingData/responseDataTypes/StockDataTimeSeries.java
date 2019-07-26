package at.uibk.apis.stocks.worldTradingData.responseDataTypes;


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class StockDataTimeSeries implements Serializable {
    private String fullyQualifiedSymbol;

    private LocalDate lastRefreshed;

    private TreeMap<LocalDate, StockDataTimeSeriesEntry> entries;

    private StockDataTimeSeriesEntry lastEntry;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private long volume;

    private BigDecimal previousClose;

    private BigDecimal change;

    private BigDecimal changePercent;

    private BigDecimal yearHigh;

    private BigDecimal yearLow;


    public StockDataTimeSeries(String fullyQualifiedSymbol, TreeMap<LocalDate, StockDataTimeSeriesEntry> entries) {
        this.fullyQualifiedSymbol = fullyQualifiedSymbol;
        this.lastRefreshed = entries.lastKey();
        this.entries = entries;

        this.lastEntry = entries.lastEntry().getValue();

        this.open = lastEntry.getOpen();
        this.high = lastEntry.getHigh();
        this.low = lastEntry.getLow();
        this.close = lastEntry.getClose();
        this.volume = lastEntry.getVolume();

        setupComplexQuoteData();
    }

    private void setupComplexQuoteData() {
        StockDataTimeSeriesEntry previousTimeSeriesEntry = getFloorEntry(lastEntry.getDate().minusDays(1));

        this.previousClose = entries.descendingMap().values().stream()
                .skip(1)
                .filter(StockDataTimeSeriesEntry -> StockDataTimeSeriesEntry.getClose().compareTo(BigDecimal.ZERO) > 0)
                .map(StockDataTimeSeriesEntry::getClose)
                .findFirst().orElseThrow(IllegalStateException::new);

        this.previousClose = previousTimeSeriesEntry.getClose();

        change = close.subtract(this.previousClose);

        changePercent = close
                .divide(this.previousClose, 2, BigDecimal.ROUND_HALF_EVEN)
                .subtract(BigDecimal.ONE);

        LocalDate yearDeadLine = lastEntry.getDate().minusWeeks(52);

        NavigableMap<LocalDate, StockDataTimeSeriesEntry> yearEntries = entries.tailMap(yearDeadLine, true);

        BigDecimal yearOpen = yearEntries.firstEntry().getValue().getOpen();
        yearHigh = yearOpen;
        yearLow = yearOpen;

        for (StockDataTimeSeriesEntry value : yearEntries.values()) {
            if (yearHigh.compareTo(value.getHigh()) < 0)
                yearHigh = value.getHigh();

            if (yearLow.compareTo(value.getLow()) > 0)
                yearLow = value.getLow();
        }
    }

    public String getFullyQualifiedSymbol() {
        return fullyQualifiedSymbol;
    }

    public String getSymbol() {
        return fullyQualifiedSymbol.split("\\.", 2)[0];
    }

    public LocalDate getLastRefreshed() {
        return lastRefreshed;
    }

    public TreeMap<LocalDate, StockDataTimeSeriesEntry> getEntries() {
        return entries;
    }

    public BigDecimal getFloorClose(LocalDate date) {
        Map.Entry<LocalDate, StockDataTimeSeriesEntry> stockTimeSeriesMapEntry = entries.floorEntry(date);
        return stockTimeSeriesMapEntry == null ? null : stockTimeSeriesMapEntry.getValue().getClose();
    }

    public StockDataTimeSeriesEntry getFloorEntry(LocalDate date) {
        Map.Entry<LocalDate, StockDataTimeSeriesEntry> stockTimeSeriesMapEntry = entries.floorEntry(date);
        return stockTimeSeriesMapEntry == null ? null : stockTimeSeriesMapEntry.getValue();
    }

    public LocalDate getFloorDate(LocalDate date){
        return entries.floorKey(date);
    }

    public LocalDate getCeilingDate(LocalDate date){
        return entries.ceilingKey(date);
    }

    public StockDataTimeSeriesEntry getCeilingEntry(LocalDate date) {
        Map.Entry<LocalDate, StockDataTimeSeriesEntry> ceilingEntry = entries.ceilingEntry(date);

        return ceilingEntry == null ? null : ceilingEntry.getValue();
    }

    public StockDataTimeSeriesEntry getEntryAt(LocalDate date) {
        return entries.get(date);
    }

    public StockDataTimeSeriesEntry getLastEntry() {
        return lastEntry;
    }

    public SortedMap<LocalDate, StockDataTimeSeriesEntry> getTailEntries(LocalDate date) {
        return entries.tailMap(date);
    }

    @Override
    public String toString() {
        return "TimeSeriesResponse [fullyQualifiedSymbol=" + fullyQualifiedSymbol + ", lastRefreshed=" + lastRefreshed
                + ", entries=" + entries + "]";
    }

    /* substitute for quotes */

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public long getVolume() {
        return volume;
    }

    public BigDecimal getPrice() {
        return close;
    }

    public BigDecimal getChange() {
        return change;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public BigDecimal getYearHigh() {
        return yearHigh;
    }

    public BigDecimal getYearLow() {
        return yearLow;
    }

    public BigDecimal getYearChange(){
        LocalDate yearDeadLine = lastEntry.getDate().minusWeeks(52);
        StockDataTimeSeriesEntry startEntry = entries.floorEntry(yearDeadLine).getValue();

        BigDecimal open = startEntry.getOpen();
        BigDecimal close = lastEntry.getClose();

        return close.divide(open, 4, RoundingMode.HALF_EVEN)
                .subtract(BigDecimal.ONE);
    }
}
