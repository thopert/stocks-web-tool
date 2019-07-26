package at.uibk.apis.stocks.alphaVantage.responseDataTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class StockTimeSeries implements Serializable {
    private String fullyQualifiedSymbol;

    private LocalDate lastRefreshed;

    private TreeMap<LocalDate, StockTimeSeriesEntry> entries;

    private StockTimeSeriesEntry lastEntry;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private BigDecimal adjustedClose;

    private BigDecimal dividendAmount;

    private BigDecimal splitCoefficient;

    private long volume;

    private BigDecimal previousClose;

    private BigDecimal change;

    private BigDecimal changePercent;

    private BigDecimal yearHigh;

    private BigDecimal yearLow;


    public StockTimeSeries(String fullyQualifiedSymbol, LocalDate lastRefreshed, TreeMap<LocalDate, StockTimeSeriesEntry> entries) {
        this.fullyQualifiedSymbol = fullyQualifiedSymbol;
        this.lastRefreshed = lastRefreshed;
        this.entries = entries;

        this.lastEntry = entries.lastEntry().getValue();

        this.open = lastEntry.getOpen();
        this.high = lastEntry.getHigh();
        this.low = lastEntry.getLow();
        this.close = lastEntry.getClose();
        this.adjustedClose = lastEntry.getAdjustedClose();
        this.dividendAmount = lastEntry.getDividendAmount();
        this.splitCoefficient = lastEntry.getSplitCoefficient();
        this.volume = lastEntry.getVolume();

        setupComplexQuoteData();
    }

    private void setupComplexQuoteData() {
        StockTimeSeriesEntry previousTimeSeriesEntry = getFloorEntry(lastEntry.getDate().minusDays(1));

        this.previousClose = entries.descendingMap().values().stream()
                .skip(1)
                .filter(stockTimeSeriesEntry -> stockTimeSeriesEntry.getClose().compareTo(BigDecimal.ZERO) > 0)
                .map(StockTimeSeriesEntry::getClose)
                .findFirst().orElseThrow(IllegalStateException::new);

        this.previousClose = previousTimeSeriesEntry.getClose();

        change = close.subtract(this.previousClose);

        changePercent = close
                .divide(this.previousClose, 4, BigDecimal.ROUND_HALF_EVEN)
                .subtract(BigDecimal.ONE);

        LocalDate yearDeadLine = lastEntry.getDate().minusWeeks(52);

        NavigableMap<LocalDate, StockTimeSeriesEntry> yearEntries = entries.tailMap(yearDeadLine, true);

        BigDecimal yearOpen = yearEntries.firstEntry().getValue().getOpen();
        yearHigh = yearOpen;
        yearLow = yearOpen;

        for (StockTimeSeriesEntry value : yearEntries.values()) {
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

    public TreeMap<LocalDate, StockTimeSeriesEntry> getEntries() {
        return entries;
    }

    public BigDecimal getFloorClose(LocalDate date) {
        Map.Entry<LocalDate, StockTimeSeriesEntry> stockTimeSeriesMapEntry = entries.floorEntry(date);
        return stockTimeSeriesMapEntry == null ? null : stockTimeSeriesMapEntry.getValue().getClose();
    }

    public StockTimeSeriesEntry getFloorEntry(LocalDate date) {
        Map.Entry<LocalDate, StockTimeSeriesEntry> stockTimeSeriesMapEntry = entries.floorEntry(date);
        return stockTimeSeriesMapEntry == null ? null : stockTimeSeriesMapEntry.getValue();
    }

    public LocalDate getFloorDate(LocalDate date){
        return entries.floorKey(date);
    }

    public LocalDate getCeilingDate(LocalDate date){
        return entries.ceilingKey(date);
    }

    public StockTimeSeriesEntry getCeilingEntry(LocalDate date) {
        Map.Entry<LocalDate, StockTimeSeriesEntry> ceilingEntry = entries.ceilingEntry(date);

        return ceilingEntry == null ? null : ceilingEntry.getValue();
    }

    public StockTimeSeriesEntry getEntryAt(LocalDate date) {
        return entries.get(date);
    }

    public StockTimeSeriesEntry getLastEntry() {
        return lastEntry;
    }

    public SortedMap<LocalDate, StockTimeSeriesEntry> getTailEntries(LocalDate date) {
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

    public BigDecimal getAdjustedClose() {
        return adjustedClose;
    }

    public BigDecimal getDividendAmount() {
        return dividendAmount;
    }

    public BigDecimal getSplitCoefficient() {
        return splitCoefficient;
    }

    public BigDecimal getYearHigh() {
        return yearHigh;
    }

    public BigDecimal getYearLow() {
        return yearLow;
    }

    public BigDecimal getYearPercentChange(){
        LocalDate yearDeadLine = lastEntry.getDate().minusWeeks(52);
        StockTimeSeriesEntry startEntry = entries.floorEntry(yearDeadLine).getValue();

        BigDecimal open = startEntry.getOpen();
        BigDecimal close = lastEntry.getClose();

        return close.divide(open, 4, RoundingMode.HALF_EVEN)
                .subtract(BigDecimal.ONE);
    }

    public BigDecimal getYearChange(){
        LocalDate yearDeadLine = lastEntry.getDate().minusWeeks(52);
        StockTimeSeriesEntry startEntry = entries.floorEntry(yearDeadLine).getValue();

        BigDecimal open = startEntry.getOpen();
        BigDecimal close = lastEntry.getClose();

        return close.subtract(open);
    }

}
