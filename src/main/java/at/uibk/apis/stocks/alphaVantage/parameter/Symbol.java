package at.uibk.apis.stocks.alphaVantage.parameter;

import java.io.Serializable;

public class Symbol implements ApiParameter, Serializable {
	private String value;
	
	public Symbol(String value) {
		this.value = value;
	}
	
	@Override
	public String getKey() {
		return "symbol";
	}

	@Override
	public String getValue() {
		return this.value;
	}

}
