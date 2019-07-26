package at.uibk.apis.exchangeRates.endpoints;

import at.uibk.apis.exchangeRates.connector.ExchangeRatesConnector;
import at.uibk.apis.exchangeRates.parsing.ExchangeRatesTimeSeriesParser;
import at.uibk.apis.exchangeRates.parsing.LatestExchangeRatesParser;
import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRates;
import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRatesTimeSeries;
import at.uibk.model.Currency;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ExchangeRatesEndPoint implements Serializable {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String LATEST_BASE = "latest";
    private static final String LATEST_QUERY = "latest?";
    private static final String HISTORY_QUERY = "history?";
    private static final Currency DEFAULT_CURRENCY =  Currency.EUR;

    private ExchangeRatesConnector connector;
    private LatestExchangeRatesParser latestParser;
    private ExchangeRatesTimeSeriesParser historyParser;

    public ExchangeRatesEndPoint(ExchangeRatesConnector connector) {
        this.connector = connector;
        this.latestParser = new LatestExchangeRatesParser();
        this.historyParser = new ExchangeRatesTimeSeriesParser();
    }

    public ExchangeRatesEndPoint() {
        this(new ExchangeRatesConnector());
    }

    public ExchangeRates getByDate(LocalDate date) {
        return getLatestResult(date.format(DATE_FORMATTER));
    }

    /* methods for retrieving latest exchange rates */
    private String currenciesToString(Currency[] currencies){
        return Arrays.stream(currencies)
                .map(currency -> "" + currency)
                .collect(Collectors.joining(","));
    }

    public ExchangeRates getLatest() {
        return getLatestResult(LATEST_BASE);
    }

    public ExchangeRates getLatestWithBase(Currency base) {
        return getLatestWithBaseAndSymbols(base);
    }

    public ExchangeRates getLatestWithSymbols(Currency... currencySymbols) {
        return getLatestWithBaseAndSymbols(DEFAULT_CURRENCY, currencySymbols);
    }

    public ExchangeRates getLatestWithBaseAndSymbols(Currency base, Currency... currencySymbols) {
        String query = LATEST_QUERY + "base=" + base;
        if (currencySymbols.length > 0)
            query += "&symbols=" + currenciesToString(currencySymbols);
        return getLatestResult(query);
    }

    private ExchangeRates getLatestResult(String query) {
        String json = connector.doRequest(query);
        return latestParser.parse(json);
    }

    /* retrieving history data */

    public ExchangeRatesTimeSeries getHistory(LocalDate start, LocalDate end) {
        return getHistoryWithBase(start, end, DEFAULT_CURRENCY);
    }

    public ExchangeRatesTimeSeries getHistoryWithSymbols(LocalDate start, LocalDate end, Currency... symbols) {
        return getHistoryWithBaseAndSymbols(start, end, DEFAULT_CURRENCY, symbols);
    }

    public ExchangeRatesTimeSeries getHistoryWithBase(LocalDate start, LocalDate end, Currency base) {
        return getHistoryWithBaseAndSymbols(start, end, base);
    }

    public ExchangeRatesTimeSeries getHistoryWithBaseAndSymbols(LocalDate start, LocalDate end, Currency base,
                                                                Currency... symbols) {
        String startAt = DATE_FORMATTER.format(start);
        String endAt = DATE_FORMATTER.format(end);
        return getHistoryResults(HISTORY_QUERY +
                "start_at=" + startAt + "&" +
                "end_at=" + endAt + "&" +
                "base=" + base +
                (symbols.length > 0 ? "&" + "symbols=" + currenciesToString(symbols) : ""));
    }

    private ExchangeRatesTimeSeries getHistoryResults(String query) {
        String json = connector.doRequest(query);
        return historyParser.parse(json);
    }


    /* some test methods */
    public static void testByDate() {
        ExchangeRatesEndPoint exr = new ExchangeRatesEndPoint();
        ExchangeRates result = exr.getByDate(LocalDate.now().minusDays(4));
        System.out.println("Base: " + result.getBase());
        System.out.println("Date: " + result.getDate());
        result.getRates().forEach((k, v) -> System.out.println(k + ": " + v));
    }

    public static void testLatest() {
        ExchangeRatesEndPoint exr = new ExchangeRatesEndPoint();
        ExchangeRates result = exr.getLatestWithBase(Currency.EUR);
        System.out.println("Original: EUR");
        System.out.println("Base: " + result.getBase());
        System.out.println("Date: " + result.getDate());
        result.getRates().forEach((k, v) -> System.out.println(k + ": " + v));

        System.out.println("Transformed: USD");
        result.resetBase(Currency.USD);
        System.out.println("Base: " + result.getBase());
        System.out.println("Date: " + result.getDate());
        result.getRates().forEach((k, v) -> System.out.println(k + ": " + v));

        System.out.println("Original: USD");
        result = exr.getLatestWithBase(Currency.USD);
        System.out.println("Base: " + result.getBase());
        System.out.println("Date: " + result.getDate());
        result.getRates().forEach((k, v) -> System.out.println(k + ": " + v));

    }

    public static void testHistory() {
        ExchangeRatesEndPoint exr = new ExchangeRatesEndPoint();
        LocalDate start = LocalDate.parse("01.10.2018", DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        LocalDate end = LocalDate.parse("10.10.2018", DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        ExchangeRatesTimeSeries result = exr.getHistoryWithSymbols(start, end, Currency.USD);
        System.out.println("Base: " + result.getBase());
        System.out.println("Start: " + result.getStart());
        System.out.println("End: " + result.getEnd());
        result.getRates().forEach((k, v) -> System.out.println(k + ": " + v));
    }

    public static void main(String[] args) {
        testLatest();
//        testHistory();
//        testByDate();
    }
}
