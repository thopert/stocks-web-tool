package at.uibk.apis.exchangeRates.parsing;

import at.uibk.apis.exchangeRates.exceptions.ExchangeRateException;
import at.uibk.model.Currency;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ParserUtils {
    final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static final Currency DEFAULT_BASE = Currency.EUR;

    private static final List<String> SUPPORTED_CURRENCIES;

    static {
        SUPPORTED_CURRENCIES = Arrays.stream(Currency.values())
                .map(Currency::toString)
                .collect(Collectors.toList());
    }

    static LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ExchangeRateException("Date format changed!");
        }
    }

    static boolean isSupportedCurrency(String currency) {
        return SUPPORTED_CURRENCIES.contains(currency);
    }
}
