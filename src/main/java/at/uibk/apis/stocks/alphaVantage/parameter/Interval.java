package at.uibk.apis.stocks.alphaVantage.parameter;

public enum Interval implements ApiParameter {
	ONE_MIN("1min"), 
	FIVE_MIN("5min"), 
	FIFTEEN_MIN("15min"), 
	THIRTY_MIN("30min"), 
	SIXTY_MIN("60min");

	private String value;

	private Interval(String value) {
		this.value = value;
	}

	@Override
	public String getKey() {
		return "interval";
	}

	@Override
	public String getValue() {
		return this.value;
	}

}
