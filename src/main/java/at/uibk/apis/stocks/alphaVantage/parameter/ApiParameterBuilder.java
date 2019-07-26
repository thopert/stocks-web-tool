package at.uibk.apis.stocks.alphaVantage.parameter;

import java.io.Serializable;

public class ApiParameterBuilder implements Serializable {
	private StringBuilder urlParametersBuilder;

	public ApiParameterBuilder() {
		this.urlParametersBuilder = new StringBuilder();
	}
	
	public ApiParameterBuilder append(ApiParameter apiParamter) {
		if(apiParamter != null) {
			this.append(apiParamter.getKey(), apiParamter.getValue());
		}
		return this;
	}

	public void append(String key, String value) {
			this.urlParametersBuilder.append("&" + key + "=" + value);
	}

	@Override
	public String toString() {
		return this.urlParametersBuilder.toString();
	}
}
