package at.uibk.producers;


import at.uibk.apis.stocks.alphaVantage.connector.AlphaVantageConnector;
import at.uibk.apis.stocks.alphaVantage.tools.AlphaVantageEndPointsTool;

import javax.enterprise.inject.Produces;

// new api-key: 86Y9PPOE04I98KE9
public class WebServiceConnectors {
    @Produces
    public AlphaVantageEndPointsTool getAlphaVantageEndPointsTool(){
        return new AlphaVantageEndPointsTool(new AlphaVantageConnector("OPZULF1UCOSLUYOV"));
    }
}
