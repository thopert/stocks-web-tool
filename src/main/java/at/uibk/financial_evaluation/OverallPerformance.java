package at.uibk.financial_evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OverallPerformance implements FinancialPerformance {
    private LocalDate deadLine;
    private BigDecimal currentValue;
    private BigDecimal totalSales;
    private BigDecimal totalDividends;
    private BigDecimal totalEarnings;
    private BigDecimal totalCosts;
    private BigDecimal totalReturn;
    private BigDecimal totalPercentReturn;
    private List<PortfolioPerformance> portfolioPerformances;

    public OverallPerformance(List<PortfolioPerformance> portfolioPerformances, LocalDate deadLine, BigDecimal currentValue,
                              BigDecimal totalSales, BigDecimal totalDividends, BigDecimal totalEarnings, BigDecimal totalCosts,
                              BigDecimal totalReturn, BigDecimal totalPercentReturn) {
        this.deadLine = deadLine;
        this.currentValue = currentValue;
        this.totalSales = totalSales;
        this.totalDividends = totalDividends;
        this.totalEarnings = totalEarnings;
        this.totalCosts = totalCosts;
        this.totalReturn = totalReturn;
        this.totalPercentReturn = totalPercentReturn;
        this.portfolioPerformances = portfolioPerformances;
    }

    public OverallPerformance() {
        this.currentValue = BigDecimal.ZERO;
        this.totalSales = BigDecimal.ZERO;
        this.totalDividends = BigDecimal.ZERO;
        this.totalEarnings = BigDecimal.ZERO;
        this.totalCosts = BigDecimal.ZERO;
        this.totalReturn = BigDecimal.ZERO;
        this.totalPercentReturn = BigDecimal.ZERO;
    }

    public OverallPerformance(List<PortfolioPerformance> portfolioPerformances) {
        this();
        this.portfolioPerformances = portfolioPerformances;
    }

    public List<PortfolioPerformance> getPortfolioPerformances() {
        return portfolioPerformances == null ? new ArrayList<>() : portfolioPerformances;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public BigDecimal getTotalDividends() {
        return totalDividends;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public BigDecimal getTotalCosts() {
        return totalCosts;
    }

    public BigDecimal getTotalReturn() {
        return totalReturn;
    }

    public BigDecimal getTotalPercentReturn() {
        return totalPercentReturn;
    }

    public LocalDate getDeadLine() {
        return deadLine;
    }

    public boolean hasProfit() {
        return totalReturn.compareTo(BigDecimal.ZERO) >= 0;
    }

    public void clearPortfolioPerformances(){
        this.portfolioPerformances = null;
    }
}
