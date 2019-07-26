package at.uibk.apis.stocks.worldTradingData.exceptions;

public class WorldTradingDataException extends RuntimeException {

	public WorldTradingDataException(){
	}

	public WorldTradingDataException(String message) {
		super(message);
	}

	public WorldTradingDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public WorldTradingDataException(Throwable cause) {
		super(cause);
	}
}
