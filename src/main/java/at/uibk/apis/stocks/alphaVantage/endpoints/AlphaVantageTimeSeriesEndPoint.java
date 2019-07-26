package at.uibk.apis.stocks.alphaVantage.endpoints;

import at.uibk.apis.stocks.alphaVantage.connector.AlphaVantageConnector;
import at.uibk.apis.stocks.alphaVantage.parameter.Function;
import at.uibk.apis.stocks.alphaVantage.parameter.OutputSize;
import at.uibk.apis.stocks.alphaVantage.parameter.Symbol;
import at.uibk.apis.stocks.alphaVantage.parsing.StockTimeSeriesParser;
import at.uibk.apis.stocks.alphaVantage.parsing.StockTimeSeriesParserType;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeries;
import at.uibk.apis.stocks.alphaVantage.transforming.TransformTimeSeries;

import java.io.Serializable;

public class AlphaVantageTimeSeriesEndPoint implements Serializable {

    private AlphaVantageConnector connector;

    public AlphaVantageTimeSeriesEndPoint(AlphaVantageConnector connector) {
        this.connector = connector;
    }

    public StockTimeSeries getDays(String fullyQualifiedSymbol, OutputSize outputSize) {
        String json = connector.doRequest(new Symbol(fullyQualifiedSymbol), Function.DAILY_ADJUSTED, outputSize);
        StockTimeSeriesParser parser = new StockTimeSeriesParser(StockTimeSeriesParserType.DAILY);
        return parser.parse(json);
    }

    public StockTimeSeries getWeeks(String fullyQualifiedSymbol) {
        String json = connector.doRequest(new Symbol(fullyQualifiedSymbol), Function.WEEKLY);
        StockTimeSeriesParser parser = new StockTimeSeriesParser(StockTimeSeriesParserType.WEEKLY);
        return parser.parse(json);
    }

    public StockTimeSeries getMonths(String fullyQualifiedSymbol) {
        String json = connector.doRequest(new Symbol(fullyQualifiedSymbol), Function.MONTHLY);
        StockTimeSeriesParser parser = new StockTimeSeriesParser(StockTimeSeriesParserType.MONTHLY);
        return parser.parse(json);
    }

    public static void main(String[] args) {
        AlphaVantageConnector connector = new AlphaVantageConnector("OPZULF1UCOSLUYOV");
        AlphaVantageTimeSeriesEndPoint endPoint = new AlphaVantageTimeSeriesEndPoint(connector);

        long start = System.currentTimeMillis();
        StockTimeSeries dayTimeSeries = endPoint.getDays("apc.ber", OutputSize.FULL);
        long time = System.currentTimeMillis() - start;

        StockTimeSeries monthTimeSeries = TransformTimeSeries.fromDailyToMonthlyTimeSeries(dayTimeSeries);

        monthTimeSeries.getEntries().forEach(
                (date, mtse) -> System.out.println(date + ": " + "open = " + mtse.getOpen() + ", high = " + mtse.getHigh()
                        + ", low = " + mtse.getLow() + " , close = " + mtse.getClose() + ", volume = " + mtse.getVolume())
        );

    }
}
