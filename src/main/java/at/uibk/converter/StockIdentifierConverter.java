package at.uibk.converter;

import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockExchange;
import at.uibk.model.mainEntities.StockIdentifier;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter(forClass = StockIdentifier.class)
public class StockIdentifierConverter implements Converter<StockIdentifier> {

    @Override
    public StockIdentifier getAsObject(FacesContext context, UIComponent component, String stockIdentifier) {
        if(stockIdentifier == null || stockIdentifier.isEmpty()){
            return null;
        }

        stockIdentifier = normalizeStockIdentifier(stockIdentifier);

        String[] identifierParts = stockIdentifier.split("\\.",2);
        if(identifierParts.length != 2)
            throw new ConverterException(new FacesMessage("Invalid stock identifier!"));
        try {
            String symbol = identifierParts[0];
            StockExchange stockExchange = StockExchange.valueOf(identifierParts[1]);
            return new StockIdentifier(symbol, stockExchange);
        } catch (IllegalArgumentException e) {
            throw new ConverterException(new FacesMessage("Invalid stock identifier!"));
        }
    }

    private String normalizeStockIdentifier(String symbol){
        return symbol.replace(" ", "").toUpperCase();
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, StockIdentifier stockIdentifier) {
        if (stockIdentifier == null)
            return "";

        return stockIdentifier.toString();
    }
}
