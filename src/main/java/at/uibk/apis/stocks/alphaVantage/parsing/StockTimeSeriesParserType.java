package at.uibk.apis.stocks.alphaVantage.parsing;

public enum StockTimeSeriesParserType {
	DAILY("Time Series (Daily)"), 
	WEEKLY("Weekly Time Series"), 
	MONTHLY("Monthly Time Series");
	
	private String key;

	StockTimeSeriesParserType(String key) {
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}

}
