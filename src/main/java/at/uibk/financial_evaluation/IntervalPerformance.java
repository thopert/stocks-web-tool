package at.uibk.financial_evaluation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class IntervalPerformance implements Serializable {
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;

    public IntervalPerformance() {
        this.open = BigDecimal.ZERO;
        this.high = BigDecimal.ZERO;
        this.low = BigDecimal.ZERO;
        this.close = BigDecimal.ZERO;
    }

    public IntervalPerformance(LocalDate fromDate, LocalDate toDate, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public IntervalPerformance(LocalDate fromDate, LocalDate toDate, BigDecimal open, BigDecimal close) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.open = open;
        this.close = close;
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
        if(high == null || low == null)
            return null;

        return high.subtract(low);
    }
}
