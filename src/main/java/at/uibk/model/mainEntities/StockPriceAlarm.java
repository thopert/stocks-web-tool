package at.uibk.model.mainEntities;

import at.uibk.model.Currency;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(name = "stockPriceAlarm")
public class StockPriceAlarm implements Serializable {
    private static final int ROUNDING_PRECISION = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Holding holding;

    @Enumerated(EnumType.STRING)
    private StockPriceAlarmType type;

    private boolean activated;

    private BigDecimal referencePrice;
    @NotNull(message = "Wert darf nicht leer sein!")
    private BigDecimal amount;

    public StockPriceAlarm() {
    }

    public StockPriceAlarm(boolean activated, BigDecimal referencePrice, Holding holding) {
        this.activated = activated;
        this.referencePrice = referencePrice;
        this.holding = holding;
        this.amount = BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "StockPriceAlarm{" +
                "id=" + id +
                ", type=" + type +
                ", activated=" + activated +
                ", referencePrice=" + referencePrice +
                ", bound=" + amount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockPriceAlarm that = (StockPriceAlarm) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() {
        return id;
    }

    public Holding getHolding() {
        return holding;
    }

    public boolean isActivated() {
        return activated;
    }

    public BigDecimal getReferencePrice() {
        return referencePrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    private BigDecimal getAmountInPercent(){
        return this.amount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);
    }

    public String getLimit(){
        if(type == StockPriceAlarmType.UPPER_LIMIT || type == StockPriceAlarmType.LOWER_LIMIT)
            return amount.toString();

        if(type == StockPriceAlarmType.UPPER_MOVE)
            return referencePrice.add(amount).toString();

        if(type == StockPriceAlarmType.UPPER_MOVE)
            return referencePrice.subtract(amount).toString();

        if(type == StockPriceAlarmType.UPPER_PERCENT_MOVE)
            return referencePrice.multiply(BigDecimal.ONE.add(getAmountInPercent())).toString();

        if(type == StockPriceAlarmType.DOWN_PERCENT_MOVE)
            return referencePrice.multiply(BigDecimal.ONE.subtract(getAmountInPercent())).toString();

        return "(" + referencePrice.multiply(BigDecimal.ONE.add(getAmountInPercent())) + ", " +
                referencePrice.multiply(BigDecimal.ONE.subtract(getAmountInPercent())).toString() + ")";

    }

    public BigDecimal[] getLowerAndUpperBound() {
        BigDecimal percentAmount = referencePrice.multiply(getAmountInPercent());
        BigDecimal upperBound = referencePrice.add(percentAmount);
        BigDecimal lowerBound = referencePrice.subtract(percentAmount);
        return new BigDecimal[]{lowerBound, upperBound};
    }

    public StockPriceAlarmType getType() {
        return type;
    }

    public User getUser() {
        if (holding == null)
            return null;

        return holding.getPortfolio().getUser();
    }

    public String getFullyQualifiedSymbol() {
        if (holding == null)
            return null;

        return holding.getFullyQualifiedSymbol();
    }

    public String getName() {
        return holding.getName();
    }

    public Currency getCurrency() {
        return holding.getCurrency();
    }

    public void setHolding(Holding holding) {
        this.holding = holding;
    }

    public void setActivated(boolean active) {
        this.activated = active;
    }

    public void setReferencePrice(BigDecimal referencePrice) {
        this.referencePrice = referencePrice;
    }

    public void setAmount(BigDecimal bound) {
        this.amount = bound;
    }

    public void setPercentBound(BigDecimal percentBound) {
        amount = percentBound.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);
    }

    public void setType(StockPriceAlarmType stockPriceAlarmType) {
        this.type = stockPriceAlarmType;
    }

    public boolean isTriggeredBy(BigDecimal currentPrice) {
        if (type == StockPriceAlarmType.UPPER_LIMIT) {
            return checkUpperLimit(amount, currentPrice);
        }

        if (type == StockPriceAlarmType.LOWER_LIMIT) {
            return checkLowerLimit(amount, currentPrice);
        }

        if(type == StockPriceAlarmType.UPPER_MOVE){
            return checkUpperMove(currentPrice);
        }

        if(type == StockPriceAlarmType.DOWN_MOVE){
            return checkDownMove(currentPrice);
        }

        if(type == StockPriceAlarmType.UPPER_PERCENT_MOVE){
            return checkUpperPercentMove(currentPrice);
        }

        if(type == StockPriceAlarmType.DOWN_PERCENT_MOVE){
            return checkDownPercentMove(currentPrice);
        }

        return checkPercentInterval(currentPrice);
    }

    private boolean checkUpperPercentMove(BigDecimal currentPrice) {
        BigDecimal upperLimit = referencePrice.multiply(BigDecimal.ONE.add(getAmountInPercent()));

        return checkUpperLimit(upperLimit, currentPrice);
    }

    private boolean checkDownPercentMove(BigDecimal currentPrice) {
        BigDecimal lowerLimit = referencePrice.multiply(BigDecimal.ONE.subtract(getAmountInPercent()));

        return checkLowerLimit(lowerLimit, currentPrice);
    }
    private boolean checkPercentInterval(BigDecimal currentPrice) {
        BigDecimal percentAmount = referencePrice.multiply(getAmountInPercent());
        BigDecimal upperBound = referencePrice.add(percentAmount);
        BigDecimal lowerBound = referencePrice.subtract(percentAmount);

        if (checkLowerLimit(lowerBound, currentPrice)) {
            return true;
        }

        if (checkUpperLimit(upperBound, currentPrice)) {
            return true;
        }

        return false;
    }

    private boolean checkDownMove(BigDecimal currentPrice) {
        BigDecimal limit = referencePrice.subtract(amount);

        if (currentPrice.compareTo(limit) <= 0) {
            return true;
        }

        return false;
    }

    private boolean checkUpperMove(BigDecimal currentPrice) {
        BigDecimal limit = referencePrice.add(amount);

        if (currentPrice.compareTo(limit) >= 0) {
            return true;
        }

        return false;
    }

    private boolean checkLowerLimit(BigDecimal lowerLimit, BigDecimal currentPrice) {
        return currentPrice.compareTo(lowerLimit) <= 0;
    }


    private boolean checkUpperLimit(BigDecimal upperLimit, BigDecimal currentPrice) {
        return currentPrice.compareTo(upperLimit) >= 0;
    }
}
