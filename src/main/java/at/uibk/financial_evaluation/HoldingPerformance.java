package at.uibk.financial_evaluation;

import at.uibk.model.mainEntities.Holding;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HoldingPerformance implements FinancialPerformance {
    private BigDecimal price;
    private BigDecimal exchangeRate;
    private BigDecimal numberOfShares;
    private BigDecimal currentValue;
    private BigDecimal totalSales;
    private BigDecimal totalDividends;
    private BigDecimal totalEarnings;
    private BigDecimal totalCosts;
    private BigDecimal totalReturn;
    private BigDecimal totalPercentReturn;
    private LocalDate deadLine;
    private Holding holding;

    public HoldingPerformance(Holding holding) {
        this.price = BigDecimal.ZERO;
        this.exchangeRate = BigDecimal.ZERO;
        this.numberOfShares = BigDecimal.ZERO;
        this.currentValue = BigDecimal.ZERO;
        this.totalSales = BigDecimal.ZERO;
        this.totalDividends = BigDecimal.ZERO;
        this.totalEarnings = BigDecimal.ZERO;
        this.totalCosts = BigDecimal.ZERO;
        this.totalReturn = BigDecimal.ZERO;
        this.totalPercentReturn = BigDecimal.ZERO;
        this.holding = holding;
    }

    public HoldingPerformance(Holding holding, LocalDate deadLine, BigDecimal price, BigDecimal exchangeRate,
                              BigDecimal numberOfShares, BigDecimal currentValue, BigDecimal totalSales, BigDecimal totalDividends,
                              BigDecimal totalEarnings, BigDecimal totalCosts, BigDecimal totalReturn, BigDecimal totalPercentReturn) {

        this.price = price;
        this.exchangeRate = exchangeRate;
        this.numberOfShares = numberOfShares;
        this.currentValue = currentValue;
        this.totalSales = totalSales;
        this.totalDividends = totalDividends;
        this.totalEarnings = totalEarnings;
        this.totalCosts = totalCosts;
        this.totalReturn = totalReturn;
        this.totalPercentReturn = totalPercentReturn;
        this.deadLine = deadLine;
        this.holding = holding;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public BigDecimal getNumberOfShares() {
        return numberOfShares;
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

    public Holding getHolding() {
        return holding;
    }

    public void setHolding(Holding holding) {
        this.holding = holding;
    }
}
