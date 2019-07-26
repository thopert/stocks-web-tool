package at.uibk.apis.stocks.alphaVantage.parsing;

import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.model.Currency;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ParserUtils {
    private static final DateTimeFormatter SIMPLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private static final List<String> SUPPORTED_CURRENCIES;

    static final LocalDate TIME_SERIES_DEADLINE = LocalDate.now().minusYears(5);

    static {
        SUPPORTED_CURRENCIES = Arrays.stream(Currency.values())
                .map(c -> c + "")
                .collect(Collectors.toList());
    }

    static LocalTime parseTime(String localTime) {
        try {
            return LocalTime.parse(localTime, TIME_FORMATTER);
        } catch(DateTimeParseException e){
            throw new AlphaVantageException("Time format changed!");
        }
    }

    static LocalDate parseSimpleDate(String localDate) {
        try {
            return LocalDate.parse(localDate, SIMPLE_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new AlphaVantageException("Date format changed!");
        }
    }

    static boolean isSupportedCurrency(String currency){
        return SUPPORTED_CURRENCIES.contains(currency);
    }

}
