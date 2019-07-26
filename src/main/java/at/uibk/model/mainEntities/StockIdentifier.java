package at.uibk.model.mainEntities;

import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockExchange;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StockIdentifier implements Serializable {

    @NotBlank(message = "Symbol darf nicht leer sein!")
    private String symbol= "";

    @Enumerated(EnumType.STRING)
    private StockExchange stockExchange = StockExchange.DEFAULT;

    public StockIdentifier() {
    }

    public StockIdentifier(StockExchange stockExchange) {
        this.stockExchange = stockExchange;
    }

    public StockIdentifier(String fullyQualifiedSymbol){
        setFullyQualifiedSymbol(fullyQualifiedSymbol);
    }

    public StockIdentifier(String symbol, String stockExchange) {
        this.symbol = normalizeSymbol(symbol);
        stockExchange = normalizeSymbol(stockExchange);
        this.stockExchange = StockExchange.valueOf(stockExchange);
    }

    public StockIdentifier(String symbol, StockExchange stockExchange) {
        this.symbol = normalizeSymbol(symbol);
        this.stockExchange = stockExchange;
    }

    /* helper */

    private String normalizeSymbol(String symbol){
        return symbol.replace(" ", "").toUpperCase();
    }

    /* getters and setters */

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = normalizeSymbol(symbol);
    }

    public StockExchange getStockExchange() {
        return stockExchange;
    }

    public void setStockExchange(StockExchange stockExchange) {
        this.stockExchange = stockExchange;
    }

    public String getFullyQualifiedSymbol(){
        if(stockExchange == null)
            return symbol;

        return symbol + stockExchange.getExtension();
    }

    public void setFullyQualifiedSymbol(String fullyQualifiedSymbol) {
        fullyQualifiedSymbol = normalizeSymbol(fullyQualifiedSymbol);

        String[] symbolParts = fullyQualifiedSymbol.split("\\.",2);

        this.symbol = symbolParts[0];

        String stockExchange = symbolParts.length < 2 ? "" : symbolParts[1];

//        this.stockExchange = stockExchange.isEmpty() ? StockExchange.US : StockExchange.valueOf(stockExchange);
    }

    public String getSearchExpression(){
        return symbol + stockExchange.getSearchExtension();
    }

    /* big three */

    @Override
    public String toString() {
        if(symbol == null || symbol.isEmpty())
            return "";

        return symbol + (stockExchange == null ? "" : ":" + stockExchange);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockIdentifier that = (StockIdentifier) o;
        return symbol.equals(that.symbol) &&
                stockExchange == that.stockExchange;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, stockExchange);
    }

}
