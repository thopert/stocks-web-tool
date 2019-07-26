package at.uibk.model.mainEntities;

import at.uibk.model.Currency;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "trade")
public class Trade implements Serializable {
    private static final long PRIMARY_KEY_NOT_SET = -1;
    private static final int DECIMAL_PLACES = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = PRIMARY_KEY_NOT_SET;

    @ManyToOne
    private Holding holding;

    @NotNull(message = "Wert ist erforderlich!")
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private TradeType type;

    @NotNull(message = "Wert ist erforderlich!")
    private BigDecimal quantity;

    @NotNull(message = "Wert ist erforderlich!")
    private BigDecimal price;

    @NotNull(message = "Wert ist erforderlich!")
    private BigDecimal brokerage;

    @Enumerated(EnumType.STRING)
    private Currency brokerageCurrency;

    @NotNull(message = "Wert ist erforderlich!")
    // direction: Portfolio currency/stock currency
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    private ExchangeRateDirection exchangeRateDirection;

    public Trade() {
    }

    public Trade(LocalDate date, TradeType type, BigDecimal quantity, BigDecimal price, BigDecimal exchangeRate, ExchangeRateDirection exchangeRateDirection, BigDecimal brokerage,
                 Currency brokerageCurrency, Holding holding) {
        this.date = date;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.brokerage = brokerage;
        this.brokerageCurrency = brokerageCurrency;
        this.exchangeRate = exchangeRate;
        this.exchangeRateDirection = exchangeRateDirection;
        this.holding = holding;
    }

    public Trade(Trade other) {
        this.id = other.id;
        this.holding = other.holding;
        this.date = other.getDate();
        this.type = other.type;
        this.quantity = other.quantity;
        this.price = other.price;
        this.brokerage = other.brokerage;
        this.brokerageCurrency = other.brokerageCurrency;
        this.exchangeRate = other.exchangeRate;
        this.exchangeRateDirection = other.exchangeRateDirection;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", date=" + date +
                ", type=" + type +
                ", quantity=" + quantity +
                ", price=" + price +
                ", brokerage=" + brokerage +
                ", brokerageCurrency=" + brokerageCurrency +
                ", exchangeRate=" + exchangeRate +
                ", exchangeRateDirection=" + exchangeRateDirection +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return id.equals(trade.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /* checking types */

    public boolean isReduction() {
        return type.isReduction();
    }

    public boolean isAdjustment() {
        return type.isAdjustment();
    }

    /* getters and setters */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Holding getHolding() {
        return holding;
    }

    public void setHolding(Holding holding) {
        this.holding = holding;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public TradeType getType() {
        return type;
    }

    public void setType(TradeType type) {
        this.type = type;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getBrokerage() {
        return brokerage;
    }

    public void setBrokerage(BigDecimal brokerage) {
        this.brokerage = brokerage;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public Currency getBrokerageCurrency() {
        return brokerageCurrency;
    }

    public List<Currency> getBrokerageCurrencies() {
        if(holding == null)
            return null;

        return holding.getBrokerageCurrencies();
    }

    public void setBrokerageCurrency(Currency brokerageCurrency) {
        this.brokerageCurrency = brokerageCurrency;
    }

    public Currency getCurrency() {
        return holding.getCurrency();
    }

    public ExchangeRateDirection getExchangeRateDirection() {
        return exchangeRateDirection;
    }

    public void setExchangeRateDirection(ExchangeRateDirection exchangeRateDirection) {
        this.exchangeRateDirection = exchangeRateDirection;
    }

    public String getExchangeRateDirectionLabel(){
        Currency portfolioCurrency = holding.getPortfolio().getCurrency();

        if(exchangeRateDirection == ExchangeRateDirection.DEFAULT){
            return portfolioCurrency + "/" + getCurrency();
        }

        return getCurrency() + "/" + portfolioCurrency;
    }

    /* some calculations */

    public BigDecimal getDefaultExchangeRate(){
        if(isAdjustment()) return BigDecimal.ZERO;

        if(exchangeRateDirection == ExchangeRateDirection.DEFAULT)
            return exchangeRate;

        return BigDecimal.ONE.divide(exchangeRate, DECIMAL_PLACES, ROUNDING_MODE);
    }

    public BigDecimal getBrokerageInHoldingCurrency() {
        if (isAdjustment()) return BigDecimal.ZERO;

        Currency holdingCurrency = getCurrency();

        if(holdingCurrency == brokerageCurrency)
            return brokerage;

        return brokerage.multiply(getDefaultExchangeRate());
    }

    public BigDecimal getBrokerageInPortfolioCurrency() {
        if (isAdjustment()) return BigDecimal.ZERO;

        Currency portfolioCurrency = holding.getPortfolio().getCurrency();

        if (portfolioCurrency == brokerageCurrency)
            return brokerage;

        return brokerage.divide(getDefaultExchangeRate(), DECIMAL_PLACES, ROUNDING_MODE);
    }

    private BigDecimal getPriceInPortfolioCurrency() {
        if (isAdjustment()) return BigDecimal.ZERO;

        return price.divide(getDefaultExchangeRate(), DECIMAL_PLACES, ROUNDING_MODE);
    }

    public BigDecimal getWorthInPortfolioCurrency() {
        if (type.isAdjustment()) return BigDecimal.ZERO;

        return quantity.multiply(getPriceInPortfolioCurrency());
    }

    public BigDecimal getCashFlowInPortfolioCurrency(){
        if(type.isAdjustment()) return BigDecimal.ZERO;

        BigDecimal totalWorth = getWorthInPortfolioCurrency();

        if(type == TradeType.SELL)
            return totalWorth.subtract(getBrokerageInPortfolioCurrency());

        return totalWorth.add(getBrokerageInPortfolioCurrency());
    }

}
