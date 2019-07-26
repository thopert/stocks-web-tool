package at.uibk.apis.stocks.alphaVantage.parameter;

public enum Function implements ApiParameter {
	QUOTE_ENDPOINT("GLOBAL_QUOTE"),
	SEARCH_ENDPOINT("SYMBOL_SEARCH"),
	DAILY("TIME_SERIES_DAILY"),
	WEEKLY("TIME_SERIES_WEEKLY"),
	MONTHLY("TIME_SERIES_MONTHLY"),
	DAILY_ADJUSTED("TIME_SERIES_DAILY_ADJUSTED");
	
	private String value;
	
	Function(String value) {
		this.value = value;
	}

	@Override
	public String getKey() {
		return "function";
	}

	@Override
	public String getValue() {
		return this.value;
	}
}
