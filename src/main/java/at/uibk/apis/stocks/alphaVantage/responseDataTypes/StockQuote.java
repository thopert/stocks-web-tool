package at.uibk.apis.stocks.alphaVantage.responseDataTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class StockQuote implements Serializable {
    private String fullyQualifiedSymbol;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal price;
    private long volume;
    private LocalDate latestTradingDay;
    private BigDecimal previousClose;
    private BigDecimal change;
    private BigDecimal changePercent;

    public String getFullyQualifiedSymbol() {
        return fullyQualifiedSymbol;
    }

    public void setFullyQualifiedSymbol(String fullyQualifiedSymbol) {
        this.fullyQualifiedSymbol = fullyQualifiedSymbol;
    }

    public String getSymbol(){
        return fullyQualifiedSymbol.split("\\.",2)[0];
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public LocalDate getLatestTradingDay() {
        return latestTradingDay;
    }

    public void setLatestTradingDay(LocalDate latestTradingDay) {
        this.latestTradingDay = latestTradingDay;
    }

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public void setPreviousClose(BigDecimal previousClose) {
        this.previousClose = previousClose;
    }

    public BigDecimal getChange() {
        return change;
    }

    public void setChange(BigDecimal change) {
        this.change = change;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
    }

    @Override
    public String toString() {
        return "QuoteResponse [fullyQualifiedSymbol=" + fullyQualifiedSymbol + "," + "\nopen=" + open + ", " + "\nhigh=" + high + ", " + "\nlow=" + low
                + ", \nprice=" + price + ", \nvolume=" + volume + ", \nlatestTradingDay=" + latestTradingDay
                + ", \npreviousClose=" + previousClose + ", \nchange=" + change + ", \nchangePercent=" + changePercent + "]";
    }

}
