package at.uibk.apis.stocks.worldTradingData.connector;


import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.apis.stocks.worldTradingData.queryParamter.ApiTokenParameter;
import at.uibk.apis.stocks.worldTradingData.queryParamter.QueryBuilder;
import at.uibk.apis.stocks.worldTradingData.queryParamter.QueryParameter;
import at.uibk.apis.stocks.worldTradingData.queryParamter.SymbolParameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class WorldTradingConnector implements Serializable {
    private static final int DEFAULT_TIMEOUT = 10_000;
    private static final String BASE_URL ="https://www.worldtradingdata.com/api/v1/";

    private ApiTokenParameter apiToken; // 16yzy5a3CnPDMBshmorkwgAYhP6YEtZd1bcb3huGtMFDFwq2wfMEIj7s6j3z

    public WorldTradingConnector(String apiToken) {
        this.apiToken = new ApiTokenParameter(apiToken);
    }

    private String getParameters(QueryParameter... queryParameters){
        QueryBuilder queryBuilder = new QueryBuilder();

        for (QueryParameter queryParameter : queryParameters) {
            queryBuilder.append(queryParameter);
        }

        queryBuilder.append(apiToken);

        return queryBuilder.toString();
    }

    public String doRequest(BaseUrlSuffix baseUrlSuffix, QueryParameter... queryParameters) {
        try {

            String endPointUrl = BASE_URL + baseUrlSuffix.getName() + "?" + getParameters(queryParameters);

            URL url = new URL(endPointUrl);

            System.out.println("Connecting: " + url.toString());

            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(DEFAULT_TIMEOUT);
            connection.setReadTimeout(DEFAULT_TIMEOUT);

            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream(),
                    StandardCharsets.UTF_8.toString());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            StringBuilder responseBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                responseBuilder.append(line);
				responseBuilder.append(System.lineSeparator());
            }
            bufferedReader.close();
            return responseBuilder.toString();
        } catch (IOException e) {
            throw new AlphaVantageException("Request not successful!", e);
        }
    }

    public static void main(String[] args) {
        WorldTradingConnector connector = new WorldTradingConnector("16yzy5a3CnPDMBshmorkwgAYhP6YEtZd1bcb3huGtMFDFwq2wfMEIj7s6j3z");

        String json = connector.doRequest(BaseUrlSuffix.STOCK, new SymbolParameter("aapl", "tsla"));

        System.out.println(json);

    }

    public static String splitStringAtLastOccurrenceOfDot(String fullyQualifiedSymbol) {
        int indexOfLastDot = fullyQualifiedSymbol.lastIndexOf(".");

        System.out.println("last index: " + indexOfLastDot);

        if (indexOfLastDot == -1)
            return fullyQualifiedSymbol;

        if (indexOfLastDot == 0)
            return "";

        return fullyQualifiedSymbol.substring(0, indexOfLastDot);

    }
}
