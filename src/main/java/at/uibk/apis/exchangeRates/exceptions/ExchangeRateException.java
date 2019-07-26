package at.uibk.apis.exchangeRates.exceptions;

public class ExchangeRateException extends RuntimeException {
    public ExchangeRateException() {
        super();
    }

    public ExchangeRateException(String message) {
        super(message);
    }

    public ExchangeRateException(String message, Throwable cause) {
        super(message, cause);
    }
}
