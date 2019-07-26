package at.uibk.csv;

import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockExchange;
import at.uibk.controller.portfolio.CurrentPortfolio;
import at.uibk.model.Currency;
import at.uibk.model.mainEntities.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.primefaces.model.UploadedFile;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CSVImporter implements Serializable {
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.withHeader().withAllowMissingColumnNames();

    private static final String[] dateTimePatterns ={"dd.MM.yyyy", "dd/MM/yyyy", "MM-dd-yyyy"};

    private List<CSVRecord> csvRecords;

    private CSVHeaders headers;

//    @Inject
//    private PortfolioManagementController portfolioManagementController;

    @Inject @CurrentPortfolio
    private Portfolio currentPortfolio;

    private static long uniqueIdCounter;

//    @PostConstruct
//    public void init() {
//        currentPortfolio = portfolioManagementController.getCurrentPortfolio();
//    }

    public CSVHeaders init(UploadedFile uploadedFile) {
        try (Reader in = new InputStreamReader(uploadedFile.getInputstream()); CSVParser parser = CSV_FORMAT.parse(in)) {

            List<CSVRecord> csvRecords = parser.getRecords();

            if (csvRecords.isEmpty()) {

                throw new EmptyCSVFileException("File contains no trades!");
            }

            this.csvRecords = csvRecords; // csvRecords is null or correct

            return CSVHeaders.of(parser.getHeaderMap().keySet());

        } catch (IOException ioe) {
            throw new CSVImporterException("I/O error reading CSV file!");
        }
    }

    private boolean isReady() {
        return csvRecords != null;
    }

    public List<Holding> importTrades(CSVHeaders headers) {
        if (!isReady() || headers == null) {
            return null;
        }

        this.headers = headers;

        Map<StockIdentifier, List<CSVRecord>> csvRecordsPerInstrument = groupTradesByStockIdentifier();

        return setupHoldingsAndTrades(csvRecordsPerInstrument);
    }

    private Map<StockIdentifier, List<CSVRecord>> groupTradesByStockIdentifier() {
        return csvRecords.stream()
                .collect(Collectors.groupingBy(this::getStockIdentifier));
    }

    private StockIdentifier getStockIdentifier(CSVRecord csvRecord) {
        String symbol = parseSymbol(csvRecord);

        StockExchange mic = parseMIC(csvRecord);

        return new StockIdentifier(symbol, mic);
    }

    private String parseSymbol(CSVRecord csvRecord) {
        String symbolHeader = headers.getName(CSVHeaderType.SYMBOL);

        if (symbolHeader != null && csvRecord.isSet(symbolHeader)) {
            String normalizeSymbol = removeWhiteSpaceAndToUpperCase(csvRecord.get(symbolHeader));
//            if (!normalizeSymbol.isEmpty())
                return normalizeSymbol;
        }
        throw new CSVImporterException("Symbols not found!");
    }

    private StockExchange parseMIC(CSVRecord csvRecord) {
        String micHeader = headers.getName(CSVHeaderType.MIC);

        if (micHeader != null && csvRecord.isSet(micHeader)) {
            String normalizedMic = removeWhiteSpaceAndToUpperCase(csvRecord.get(micHeader));
            try {
                return StockExchange.ofMIC(normalizedMic);
            } catch (IllegalArgumentException e) {
            }
        }
        return StockExchange.DEFAULT;
    }

    private String removeWhiteSpaceAndToUpperCase(String string) {
        return string.replace(" ", "").toUpperCase();
    }

    private String removeWhiteSpace(String string) {
        return string.replace(" ", "");
    }

    private List<Holding> setupHoldingsAndTrades(Map<StockIdentifier, List<CSVRecord>> csvRecordsPerSymbol) {

        List<Holding> holdings = new ArrayList<>();

        uniqueIdCounter = 0;

        for (Map.Entry<StockIdentifier, List<CSVRecord>> recordsPerSymbol : csvRecordsPerSymbol.entrySet()) {
            StockIdentifier stockIdentifier = recordsPerSymbol.getKey();

            Holding holding = new Holding(stockIdentifier, currentPortfolio);

            holding.setId(uniqueIdCounter++);

            for (CSVRecord csvRecord : recordsPerSymbol.getValue()) {
                setupTrade(csvRecord, holding);
            }

            holdings.add(holding);
        }

        return holdings;
    }

    private void setupTrade(CSVRecord csvRecord, Holding holding) {

        LocalDate date = parseDate(csvRecord);

        BigDecimal price = parseNumber(csvRecord, CSVHeaderType.PRICE);

        BigDecimal quantity = parseNumber(csvRecord, CSVHeaderType.QUANTITY);

        BigDecimal brokerage = parseNumber(csvRecord, CSVHeaderType.BROKERAGE);

        BigDecimal exchangeRate = parseNumber(csvRecord, CSVHeaderType.EXCHANGE_RATE);

        TradeType type = parseTradeType(csvRecord);

        Currency brokerageCurrency = parseBrokerageCurrency(csvRecord, holding);

//        ExchangeRateDirection exchangeRateDirection = parseExchangeRateDirection(csvRecord);

        Trade trade = new Trade (date, type, quantity, price, exchangeRate, ExchangeRateDirection.DEFAULT, brokerage, brokerageCurrency, holding);

        trade.setId(uniqueIdCounter++);

        holding.addTrade(trade);
    }

    private Currency parseBrokerageCurrency(CSVRecord csvRecord, Holding holding) {
        String brokerageCurrencyHeader = headers.getName(CSVHeaderType.BROKERAGE_CURRENCY);
        if (brokerageCurrencyHeader != null && csvRecord.isSet(brokerageCurrencyHeader)) {
            try {
                String currencyString = removeWhiteSpaceAndToUpperCase(csvRecord.get(brokerageCurrencyHeader));
                Currency importedCurrency = Currency.valueOf(currencyString);
                if (holding.getBrokerageCurrencies().contains(importedCurrency)) {
                    return importedCurrency;
                }

            } catch (IllegalArgumentException e) {
            }
        }
        return currentPortfolio.getCurrency();
    }

    private TradeType parseTradeType(CSVRecord csvRecord) {
        String typeHeader = headers.getName(CSVHeaderType.TYPE);
        if (typeHeader != null && csvRecord.isSet(typeHeader)) {
            try {
                String tradeTypeString = removeWhiteSpaceAndToUpperCase(csvRecord.get(typeHeader));
                return TradeType.valueOf(tradeTypeString);
            } catch (IllegalArgumentException e) {
            }
        }

        return TradeType.DEFAULT;
    }

    private BigDecimal parseNumber(CSVRecord csvRecord, CSVHeaderType csvHeaderType) {
        String numberHeader = headers.getName(csvHeaderType);

        if (numberHeader != null && csvRecord.isSet(numberHeader)) {
            try {
                String numberString = removeWhiteSpace(csvRecord.get(numberHeader));
                return new BigDecimal(numberString);
            } catch (NumberFormatException e) {
            }
        }
        if (csvHeaderType == CSVHeaderType.QUANTITY || csvHeaderType == CSVHeaderType.EXCHANGE_RATE)
            return BigDecimal.ONE;

        return BigDecimal.ZERO;
    }

    private LocalDate parseDate(CSVRecord csvRecord) {
        String dateHeader = headers.getName(CSVHeaderType.DATE);

        if (dateHeader != null && csvRecord.isSet(dateHeader)) {
            for (String dateTimePattern: dateTimePatterns) {
                try {
                    return LocalDate.parse(csvRecord.get(dateHeader), DateTimeFormatter.ofPattern(dateTimePattern));
                } catch (DateTimeParseException e) {
                }
            }
        }
        return LocalDate.now();
    }

//    private ExchangeRateDirection parseExchangeRateDirection(CSVRecord csvRecord) {
//        String exchangeRateDirectionHeader = headers.getName(CSVHeaderType.EXCHANGE_RATE_DIRECTION);
//
//        if (exchangeRateDirectionHeader == null || !csvRecord.isSet(exchangeRateDirectionHeader)) {
//            return ExchangeRateDirection.DEFAULT;
//        }
//
//        String exchangeRateDirection = csvRecord.get(exchangeRateDirectionHeader).trim();
//
//        String[] fromToCurrencies = exchangeRateDirection.split("\\/");
//
//        if (fromToCurrencies.length != 2)
//            return ExchangeRateDirection.DEFAULT;
//
//        try {
//            Currency fromCurrency = Currency.valueOf(fromToCurrencies[0]);
//            if (fromCurrency == currentPortfolio.getCurrency()) {
//                return ExchangeRateDirection.DEFAULT;
//            } else{
//                return ExchangeRateDirection.REVERSE;
//            }
//        } catch (IllegalArgumentException e) { }
//
//        return ExchangeRateDirection.DEFAULT;
//    }

    public static void main(String[] args) {
        String first = "".split("\\/")[0];

        System.out.println(TradeType.valueOf("BUY"));


    }
}
