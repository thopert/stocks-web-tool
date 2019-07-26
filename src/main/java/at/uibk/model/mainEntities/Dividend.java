package at.uibk.model.mainEntities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "dividend")
public class Dividend implements Serializable {
    private static final int DECIMAL_PLACES = 4;
    private static final RoundingMode HALF_EVEN = RoundingMode.HALF_EVEN;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "Wert ist erforderlich!")
    private LocalDate date;
    @NotNull(message = "Wert ist erforderlich!")
    private BigDecimal amount;
    @NotNull(message = "Wert ist erforderlich!")
    private BigDecimal exchangeRate;
    @Enumerated(EnumType.STRING)
    private ExchangeRateDirection exchangeRateDirection;

    @ManyToOne
    private Holding holding;

    public Dividend() {
    }

    public Dividend(Dividend other) {
        this.id = other.id;
        this.date = other.date;
        this.amount = other.amount;
        this.exchangeRate = other.exchangeRate;
        this.exchangeRateDirection = other.exchangeRateDirection;
        this.holding = other.holding;
    }

    public Dividend(LocalDate date, BigDecimal amount, BigDecimal exchangeRate, Holding holding) {
        this.date = date;
        this.amount = amount;
        this.exchangeRate = exchangeRate;
        this.exchangeRateDirection = ExchangeRateDirection.DEFAULT;
        this.holding = holding;
    }

    public Dividend(Holding holding) {
        this.date = LocalDate.now();
        this.amount = BigDecimal.ZERO;
        this.exchangeRate = BigDecimal.ONE;
        this.exchangeRateDirection = ExchangeRateDirection.DEFAULT;
        this.holding = holding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dividend dividend = (Dividend) o;
        return id.equals(dividend.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Dividend{" +
                "id=" + id +
                ", date=" + date +
                ", amount=" + amount +
                ", exchangeRate=" + exchangeRate +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public Holding getHolding() {
        return holding;
    }

    public void setHolding(Holding holding) {
        this.holding = holding;
    }

    public ExchangeRateDirection getExchangeRateDirection() {
        return exchangeRateDirection;
    }

    public void setExchangeRateDirection(ExchangeRateDirection exchangeRateDirection) {
        this.exchangeRateDirection = exchangeRateDirection;
    }

    public BigDecimal getDefaultExchangeRate() {
        if (exchangeRateDirection == ExchangeRateDirection.DEFAULT)
            return exchangeRate;

        return BigDecimal.ONE.divide(exchangeRate, DECIMAL_PLACES, HALF_EVEN);
    }

    public BigDecimal getAmountInPortfolioCurrency() {
        if (exchangeRate != null && exchangeRate.compareTo(BigDecimal.ZERO) != 0)
            return amount.divide(getDefaultExchangeRate(), DECIMAL_PLACES, HALF_EVEN);

        return BigDecimal.ZERO;
    }

}
