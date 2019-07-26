package at.uibk.apis.stocks.worldTradingData.queryParamter;

public class ApiTokenParameter implements QueryParameter {
    private String apiToken;

    public ApiTokenParameter(String apiToken) {
        this.apiToken = apiToken;
    }

    @Override
    public String getKey() {
        return "api_token";
    }

    @Override
    public String getValue() {
        return apiToken;
    }
}
