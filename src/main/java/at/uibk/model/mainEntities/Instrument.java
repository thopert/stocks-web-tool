package at.uibk.model.mainEntities;

import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockExchange;
import at.uibk.model.Currency;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "instrument")
public class Instrument implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded @AttributeOverride(name = "stockExchange", column = @Column(name = "exchange"))
    private StockIdentifier stockIdentifier;

    private String name;
    private String isin;
    private String wkn;

    /* getters and setters */

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return stockIdentifier.getSymbol();
    }

    public StockExchange getExchange() {
        return stockIdentifier.getStockExchange();
    }

    public String getIsin() {
        return isin;
    }

    public String getWkn() {
        return wkn;
    }

    public Currency getCurrency() {
        return stockIdentifier.getStockExchange().getCurrency();
    }

    public StockExchange getStockExchange(){
        return stockIdentifier.getStockExchange();
    }

    public String getFullyQualifiedSymbol(){
        return stockIdentifier.getFullyQualifiedSymbol();
    }

    public StockIdentifier getStockIdentifier() {
        return stockIdentifier;
    }

    /* big three */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instrument that = (Instrument) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Instrument{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", stockIdentifier=" + stockIdentifier +
                ", isin='" + isin + '\'' +
                ", wkn='" + wkn + '\'' +
                '}';
    }
}
