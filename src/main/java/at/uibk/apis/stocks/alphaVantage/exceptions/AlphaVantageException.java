package at.uibk.apis.stocks.alphaVantage.exceptions;

public class AlphaVantageException extends RuntimeException {

	public AlphaVantageException(){
	}

	public AlphaVantageException(String message) {
		super(message);
	}

	public AlphaVantageException(String message, Throwable cause) {
		super(message, cause);
	}

	public AlphaVantageException(Throwable cause) {
		super(cause);
	}
}
