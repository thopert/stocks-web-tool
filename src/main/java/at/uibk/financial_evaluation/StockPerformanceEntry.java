package at.uibk.financial_evaluation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class StockPerformanceEntry implements Serializable {
    private int monthsBack;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private long volume;


    public StockPerformanceEntry() {
        this.open = BigDecimal.ZERO;
        this.high = BigDecimal.ZERO;
        this.low = BigDecimal.ZERO;
        this.close = BigDecimal.ZERO;
    }

    public StockPerformanceEntry(int monthsBack, LocalDate fromDate, LocalDate toDate, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, long volume) {
        this.monthsBack = monthsBack;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public int getMonthsBack() {
        return monthsBack;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

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

    public BigDecimal getChange(){
        return close.subtract(open);
    }

    public BigDecimal getPercentChange(){
        if(open.compareTo(BigDecimal.ZERO) == 0)
            return null;

        return close.divide(open, 4, BigDecimal.ROUND_HALF_EVEN).subtract(BigDecimal.ONE);
    }

    public BigDecimal getConvertedPercentChange(){
        return getPercentChange().multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getRange(){
        return high.subtract(low);
    }

    public String getLabel(){
        String labelUnit = monthsBack >= 12 ? "Jahr" : "Monat";

        int realAmount = monthsBack >= 12 ? monthsBack/12 : monthsBack;

        String labelUnitSuffix = realAmount > 1 ? "e" : "";

        return realAmount + " " + labelUnit + labelUnitSuffix;
    }
}
