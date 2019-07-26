package at.uibk.apis.stocks.alphaVantage.exceptions;

public class CallFrequencyExceededException extends AlphaVantageException {

    public CallFrequencyExceededException() {
        this("Call frequency exceeded!");
    }

    public CallFrequencyExceededException(String message) {
        super(message);
    }

    public CallFrequencyExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
