package at.uibk.apis.stocks.worldTradingData.responseDataTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockData implements Serializable {
    private String fullyQualifiedSymbol;
    private BigDecimal price;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal yearHigh;
    private BigDecimal yearLow;
    private LocalDateTime lastTradeTime;
    private BigDecimal dayChange;
    private BigDecimal changePercent;
    private BigDecimal previousClose;

    public StockData(String fullyQualifiedSymbol, BigDecimal price, BigDecimal open, BigDecimal high, BigDecimal low,
                     BigDecimal yearHigh, BigDecimal yearLow, LocalDateTime lastTradeTime, BigDecimal dayChange,
                     BigDecimal changePercent, BigDecimal previousClose, long volume) {
        this.fullyQualifiedSymbol = fullyQualifiedSymbol;
        this.price = price;
        this.open = open;
        this.high = high;
        this.low = low;
        this.yearHigh = yearHigh;
        this.yearLow = yearLow;
        this.lastTradeTime = lastTradeTime;
        this.dayChange = dayChange;
        this.changePercent = changePercent;
        this.previousClose = previousClose;
        this.volume = volume;
    }

    private long volume;

    public String getFullyQualifiedSymbol() {
        return fullyQualifiedSymbol;
    }

    public void setFullyQualifiedSymbol(String fullyQualifiedSymbol) {
        this.fullyQualifiedSymbol = fullyQualifiedSymbol;
    }

    public String getSymbol() {
//        return fullyQualifiedSymbol.split("\\.", 2)[0];
        return splitStringAtLastOccurrenceOfDot(fullyQualifiedSymbol);
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public BigDecimal getYearHigh() {
        return yearHigh;
    }

    public void setYearHigh(BigDecimal yearHigh) {
        this.yearHigh = yearHigh;
    }

    public BigDecimal getYearLow() {
        return yearLow;
    }

    public void setYearLow(BigDecimal yearLow) {
        this.yearLow = yearLow;
    }

    public LocalDateTime getLastTradeTime() {
        return lastTradeTime;
    }

    public void setLastTradeTime(LocalDateTime lastTradeTime) {
        this.lastTradeTime = lastTradeTime;
    }

    public BigDecimal getDayChange() {
        return dayChange;
    }

    public void setDayChange(BigDecimal dayChange) {
        this.dayChange = dayChange;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
    }

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public void setPreviousClose(BigDecimal previousClose) {
        this.previousClose = previousClose;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "StockData{" +
                "fullyQualifiedSymbol='" + fullyQualifiedSymbol + '\'' +
                ", price=" + price +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", yearHigh=" + yearHigh +
                ", yearLow=" + yearLow +
                ", lastTradeTime=" + lastTradeTime +
                ", dayChange=" + dayChange +
                ", changePercent=" + changePercent +
                ", previousClose=" + previousClose +
                ", volume=" + volume +
                '}';
    }


    /* helper */

    public static String splitStringAtLastOccurrenceOfDot(String fullyQualifiedSymbol) {
        int indexOfLastDot = fullyQualifiedSymbol.lastIndexOf(".");

        if (indexOfLastDot == -1)
            return fullyQualifiedSymbol;

        if (indexOfLastDot == 0)
            return "";

        return fullyQualifiedSymbol.substring(0, indexOfLastDot);
    }


}
