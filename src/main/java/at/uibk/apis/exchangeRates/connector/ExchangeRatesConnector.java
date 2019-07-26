package at.uibk.apis.exchangeRates.connector;

import at.uibk.apis.exchangeRates.exceptions.ExchangeRateException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class ExchangeRatesConnector implements Serializable {
    private static final String BASE = "https://api.exchangeratesapi.io/";
    private static final int DEFAULT_TIMEOUT = 10_000;
    private int timeOut;

    public ExchangeRatesConnector(int timeOut) {
        this.timeOut = timeOut;
    }

    public ExchangeRatesConnector() {
        this.timeOut = DEFAULT_TIMEOUT;
    }

    public String doRequest(String query) {
        String endPoint = BASE + query;
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
            StringBuilder sb = new StringBuilder();
            while((line = bufferedReader.readLine()) != null) {
                sb.append(line);
//                sb.append(System.lineSeparator());
            }
            bufferedReader.close();
            return sb.toString();
        } catch (IOException e) {
           throw new ExchangeRateException("Error in request!");
        }
    }

    public static void main(String[] args) {
        ExchangeRatesConnector erc = new ExchangeRatesConnector();
        System.out.println(new StringBuilder().toString() == null) ;
    }
}
