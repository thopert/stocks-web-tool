package at.uibk.financial_evaluation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PortfolioIntervalPerformance implements Serializable {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal open;
    private BigDecimal close;

    public PortfolioIntervalPerformance(LocalDate startDate, LocalDate endDate, BigDecimal open, BigDecimal close) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.open = open;
        this.close = close;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getClose() {
        return close;
    }

    public BigDecimal getChange(){
        return close.subtract(open);
    }

    public BigDecimal getChangePercent(){
        return getChange().divide(open, 4, BigDecimal.ROUND_HALF_EVEN);
    }
}
