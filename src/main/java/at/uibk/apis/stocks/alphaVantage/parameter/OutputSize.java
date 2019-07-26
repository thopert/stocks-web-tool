package at.uibk.apis.stocks.alphaVantage.parameter;


public enum OutputSize implements ApiParameter {
	FULL("full"),
	COMPACT("compact");
	
	private String value;
	
	
	private OutputSize(String value) {
		this.value = value;
	}
	
	@Override
	public String getKey() {
		return "outputsize";
	}

	@Override
	public String getValue() {
		return this.value;
	}

}
