package at.uibk.apis.stocks.worldTradingData.endpoints;

import at.uibk.apis.stocks.worldTradingData.connector.BaseUrlSuffix;
import at.uibk.apis.stocks.worldTradingData.connector.WorldTradingConnector;
import at.uibk.apis.stocks.worldTradingData.parser.StockDataParser;
import at.uibk.apis.stocks.worldTradingData.queryParamter.SymbolParameter;
import at.uibk.apis.stocks.worldTradingData.responseDataTypes.StockData;

import java.io.Serializable;
import java.util.List;

public class StockDataEndPoint implements Serializable {

    private StockDataParser stockDataParser = new StockDataParser();

    private WorldTradingConnector connector;

    public StockDataEndPoint(WorldTradingConnector connector) {
        this.connector = connector;
    }

    public StockData getStockData(String symbol) {
        SymbolParameter symbolParameter = new SymbolParameter(symbol);

        String json = connector.doRequest(BaseUrlSuffix.STOCK, symbolParameter);

        List<StockData> stockDataList = stockDataParser.parse(json);

        return stockDataList.isEmpty() ? null : stockDataList.get(0);
    }

    public static void main(String[] args) {
        WorldTradingConnector connector = new WorldTradingConnector(
                "16yzy5a3CnPDMBshmorkwgAYhP6YEtZd1bcb3huGtMFDFwq2wfMEIj7s6j3z");

        long start = System.currentTimeMillis();

        StockDataEndPoint stockDataEndPoint = new StockDataEndPoint(connector);

        StockData aapl = stockDataEndPoint.getStockData("aapl");

        long end = System.currentTimeMillis();

        System.out.println("time: " + (end - start) + "ms");

        System.out.println(aapl);


    }
}
