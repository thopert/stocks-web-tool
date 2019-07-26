package at.uibk.model.mainEntities;

import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockExchange;
import at.uibk.model.Currency;
import at.uibk.utils.TradeUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "holding")
@NamedEntityGraph(name = "HoldingWithTradesDividends",
        attributeNodes = {@NamedAttributeNode("trades"), @NamedAttributeNode("dividends")})
public class Holding implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private StockIdentifier stockIdentifier;

    private String name;

    @ManyToOne
    private Portfolio portfolio;

    @OneToMany(mappedBy = "holding", cascade = CascadeType.ALL)
    private List<Trade> trades;

    @OneToMany(mappedBy = "holding", cascade = CascadeType.ALL)
    private List<Dividend> dividends;


    /* constructors */

    public Holding() {
    }

    public Holding(StockIdentifier stockIdentifier, Portfolio portfolio) {
        this.stockIdentifier = stockIdentifier;
        this.portfolio = portfolio;
    }

//    public Holding(StockIdentifier stockIdentifier) {
//        this.stockIdentifier = stockIdentifier;
//    }

    public Holding(Instrument instrument, Portfolio portfolio) {
        this.portfolio = portfolio;
        this.stockIdentifier = instrument.getStockIdentifier();
        this.name = instrument.getName();
    }

//    public Holding(String symbol, StockExchange stockExchange) {
//        this.stockIdentifier = new StockIdentifier(symbol, stockExchange);
//    }

    /* big three */

    @Override
    public String toString() {
        return "Holding{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", stockIdentifier=" + stockIdentifier +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Holding holding = (Holding) o;
        return id.equals(holding.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void addTrade(Trade trade) {
        if (trade == null)
            return;
        if (trades == null) {
            trades = new ArrayList<>();
        }
        trades.add(trade);
    }

    public void addDividend(Dividend dividend) {
        if (dividend == null){
            return;
        }
        if (dividends == null) {
            dividends = new ArrayList<>();
        }
        dividends.add(dividend);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return stockIdentifier.getSymbol();
    }

    public void setSymbol(String symbol) {
        this.stockIdentifier.setSymbol(symbol);
    }

    public String getFullyQualifiedSymbol() {
        return this.stockIdentifier.getFullyQualifiedSymbol();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Currency getCurrency() {
        return getStockExchange().getCurrency();
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public List<Trade> getTrades() {
        return trades == null ? new ArrayList<>() : trades;
    }

    public boolean hasTrades(){
        return trades == null || trades.isEmpty() ? false : true;
    }

    public List<Trade> getTradesUntil(LocalDate deadLine) {
        if (trades == null)
            return null;

        return trades.stream()
                .filter(trade -> trade.getDate().compareTo(deadLine) <= 0)
                .collect(Collectors.toList());
    }

    public void setTrades(List<Trade> trades) {
        this.trades = trades;
    }

    public StockExchange getStockExchange() {
        return stockIdentifier.getStockExchange();
    }

    public void setStockExchange(StockExchange stockExchange) {
        this.stockIdentifier.setStockExchange(stockExchange);
    }

    public StockIdentifier getStockIdentifier() {
        return stockIdentifier;
    }

    public void setStockIdentifier(StockIdentifier stockIdentifier) {
        this.stockIdentifier = stockIdentifier;
    }

    public List<Dividend> getDividends() {
        return dividends == null ? Collections.EMPTY_LIST : dividends;
    }

    public List<Dividend> getDividendsUntil(LocalDate deadline) {
        if (dividends == null) return null;

        return dividends.stream()
                .filter(dividend -> dividend.getDate().compareTo(deadline) <= 0)
                .collect(Collectors.toList());
    }

    public void setDividends(List<Dividend> dividends) {
        this.dividends = dividends;
    }

    public boolean hasDividends(){
        return dividends == null || dividends.isEmpty() ? false : true;
    }

    public boolean isClosed() {
        return trades == null ? true : TradeUtils.isClosedPosition(trades);
    }

    public BigDecimal numberOfShares() {
        if(trades == null) return BigDecimal.ZERO;

        return TradeUtils.getNumberOfShares(trades);
    }

    public boolean hasPortfolioCurrency() {
        return getCurrency() == portfolio.getCurrency();
    }

    public LocalDate getFirstTradeDate() {
        if (trades == null || trades.isEmpty())
            return null;

        return trades.stream()
                .map(Trade::getDate)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalStateException("One trade must exist!"));
    }

    public List<Currency> getBrokerageCurrencies() {
        if(portfolio == null)
            return new ArrayList<>();

        Currency holdingCurrency = getCurrency();
        Currency portfolioCurrency = portfolio.getCurrency();

        if (holdingCurrency == portfolioCurrency)
            return Arrays.asList(holdingCurrency);

        return Arrays.asList(holdingCurrency, portfolioCurrency);
    }
}

