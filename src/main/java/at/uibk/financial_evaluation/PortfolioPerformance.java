package at.uibk.financial_evaluation;

import at.uibk.model.mainEntities.Portfolio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PortfolioPerformance implements FinancialPerformance {
    private LocalDate deadLine;
    private BigDecimal currentValue;
    private BigDecimal totalSales;
    private BigDecimal totalDividends;
    private BigDecimal totalEarnings;
    private BigDecimal totalCosts;
    private BigDecimal totalReturn;
    private BigDecimal totalPercentReturn;
    private List<HoldingPerformance> holdingPerformances;
    private Portfolio portfolio;

    public PortfolioPerformance(Portfolio portfolio, List<HoldingPerformance> holdingPerformances, LocalDate deadLine, BigDecimal currentValue,
                                BigDecimal totalSales, BigDecimal totalDividends, BigDecimal totalEarnings, BigDecimal totalCosts,
                                BigDecimal totalReturn, BigDecimal totalPercentReturn) {
        this.portfolio = portfolio;
        this.holdingPerformances = holdingPerformances;
        this.deadLine = deadLine;
        this.currentValue = currentValue;
        this.totalSales = totalSales;
        this.totalDividends = totalDividends;
        this.totalEarnings = totalEarnings;
        this.totalCosts = totalCosts;
        this.totalReturn = totalReturn;
        this.totalPercentReturn = totalPercentReturn;
    }

    public PortfolioPerformance(Portfolio portfolio) {
        this.currentValue = BigDecimal.ZERO;
        this.totalSales = BigDecimal.ZERO;
        this.totalDividends = BigDecimal.ZERO;
        this.totalEarnings = BigDecimal.ZERO;
        this.totalCosts = BigDecimal.ZERO;
        this.totalReturn = BigDecimal.ZERO;
        this.totalPercentReturn = BigDecimal.ZERO;
        this.portfolio = portfolio;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public List<HoldingPerformance> getHoldingPerformances() {
        return holdingPerformances == null ? new ArrayList<>() : holdingPerformances;
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

    public BigDecimal getTotalPercentReturnConverted() {
        return totalPercentReturn.multiply(BigDecimal.valueOf(100));
    }

    public LocalDate getDeadLine() {
        return deadLine;
    }

    public boolean hasProfit() {
        return totalReturn.compareTo(BigDecimal.ZERO) >= 0;
    }

    public void clearHoldingPerformances(){
        this.holdingPerformances = null;
    }
}
