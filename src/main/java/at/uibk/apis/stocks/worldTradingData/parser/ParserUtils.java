package at.uibk.apis.stocks.worldTradingData.parser;

import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.apis.stocks.worldTradingData.exceptions.WorldTradingDataException;
import at.uibk.model.Currency;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ParserUtils {
    private static final DateTimeFormatter SIMPLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final List<String> SUPPORTED_CURRENCIES;

    static final LocalDate TIME_SERIES_DEADLINE = LocalDate.now().minusYears(5);

    static {
        SUPPORTED_CURRENCIES = Arrays.stream(Currency.values())
                .map(c -> c + "")
                .collect(Collectors.toList());
    }

    static LocalDate parseSimpleDate(String localDate) {
        try {
            return LocalDate.parse(localDate, SIMPLE_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new AlphaVantageException("Date format changed!");
        }
    }

    static LocalDateTime parseDateTime(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new WorldTradingDataException("Date format changed!");
        }
    }

    static boolean isSupportedCurrency(String currency){
        return SUPPORTED_CURRENCIES.contains(currency);
    }
}
