package at.uibk.apis.stocks.alphaVantage.parameter;

import java.io.Serializable;

public class ApiKey implements ApiParameter, Serializable {
	private String value;

	public ApiKey(String value) {
		this.value = value;
	}

	@Override
	public String getKey() {
		return "apikey";
	}

	@Override
	public String getValue() {
		return this.value;
	}

}
