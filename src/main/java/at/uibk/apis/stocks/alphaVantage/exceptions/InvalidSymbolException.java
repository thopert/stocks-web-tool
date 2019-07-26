package at.uibk.apis.stocks.alphaVantage.exceptions;

public class InvalidSymbolException extends AlphaVantageException{

    public InvalidSymbolException(){
        this("Invalid symbol!");
    }

    public InvalidSymbolException(String message) {
        super(message);
    }

    public InvalidSymbolException(String message, Throwable cause) {
        super(message, cause);
    }
}
