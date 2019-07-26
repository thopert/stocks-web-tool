package at.uibk.apis.stocks.alphaVantage.connector;


import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.apis.stocks.alphaVantage.parameter.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class AlphaVantageConnector implements Serializable {
	private static final String BASE = "https://www.alphavantage.co/query?";
	private static final int DEFAULT_TIMEOUT = 10_000;

	private int timeOut;
	private ApiKey apiKey;
	
	public AlphaVantageConnector(String apiKey, int timeOut) {
		this.timeOut = timeOut;
		this.apiKey = new ApiKey(apiKey);
	}
	
	public AlphaVantageConnector(String apiKey) {
		this(apiKey, DEFAULT_TIMEOUT);
	}
	
	private String getParameters(ApiParameter... parameters) {
		ApiParameterBuilder urlParameters = new ApiParameterBuilder();
		for(ApiParameter ap: parameters) {
			urlParameters.append(ap);
		}
		urlParameters.append(apiKey);
		return urlParameters.toString();
	}
	
	public String doRequest(ApiParameter... parameters) {
		String endPoint = BASE + this.getParameters(parameters);
		try {
			URL url = new URL(endPoint);
			System.out.println("Connecting: " + url.toString());
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(this.timeOut);
			connection.setReadTimeout(this.timeOut);
			InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream(),
					StandardCharsets.UTF_8.toString());
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String line;
			StringBuilder responseBuilder = new StringBuilder();
			while((line = bufferedReader.readLine()) != null) {
				responseBuilder.append(line);
//				responseBuilder.append(System.lineSeparator());
			}
			bufferedReader.close();
			return responseBuilder.toString();
		} catch (IOException e) {
			throw new AlphaVantageException("Request not successful!", e);
		}
	}

    public static void main(String[] args) {
        AlphaVantageConnector avc = new AlphaVantageConnector("OPZULF1UCOSLUYOV", 10000);
        long start = System.currentTimeMillis();
        String json = avc.doRequest(new Symbol("AAPL"), Function.DAILY, OutputSize.COMPACT);
		String json2 = avc.doRequest(new Symbol("AAPL"), Function.DAILY, OutputSize.COMPACT);
		long time = System.currentTimeMillis() - start;
        System.out.println("millis: " + time);
        System.out.println(json);

    }
}
