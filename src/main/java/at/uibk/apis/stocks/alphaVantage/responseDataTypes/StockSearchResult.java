package at.uibk.apis.stocks.alphaVantage.responseDataTypes;

import at.uibk.model.Currency;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

public class StockSearchResult implements Serializable {
    private String fullyQualifiedSymbol;
    private String name;
    private String type;
    private String region;
    private LocalTime marketOpen;
    private LocalTime marketClose;
    private String timeZone;
    private Currency currency;
    private double matchScore;

    public String getFullyQualifiedSymbol() {
        return fullyQualifiedSymbol;
    }

    public String getSymbol(){
        return fullyQualifiedSymbol.split("\\.",2)[0];
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getRegion() {
        return region;
    }

    public LocalTime getMarketOpen() {
        return marketOpen;
    }

    public LocalTime getMarketClose() {
        return marketClose;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public Currency getCurrency() {
        return currency;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public void setFullyQualifiedSymbol(String fullyQualifiedSymbol) {
        this.fullyQualifiedSymbol = fullyQualifiedSymbol;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setMarketOpen(LocalTime marketOpen) {
        this.marketOpen = marketOpen;
    }

    public void setMarketClose(LocalTime marketClose) {
        this.marketClose = marketClose;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockSearchResult that = (StockSearchResult) o;
        return Objects.equals(fullyQualifiedSymbol, that.fullyQualifiedSymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullyQualifiedSymbol);
    }

    @Override
    public String toString() {
        return "SearchResponse{" +
                "fullyQualifiedSymbol='" + fullyQualifiedSymbol + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", region='" + region + '\'' +
                ", marketOpen=" + marketOpen +
                ", marketClose=" + marketClose +
                ", timeZone='" + timeZone + '\'' +
                ", currency='" + currency + '\'' +
                ", matchScore=" + matchScore +
                '}';
    }
}
