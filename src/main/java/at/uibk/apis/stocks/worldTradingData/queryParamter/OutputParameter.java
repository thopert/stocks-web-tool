package at.uibk.apis.stocks.worldTradingData.queryParamter;

public enum OutputParameter implements QueryParameter {
    CSV("csv"),
    JSON("json");

    OutputParameter(String value) {
        this.value = value;
    }

    private String value;

    @Override
    public String getKey() {
        return "output";
    }

    @Override
    public String getValue() {
        return value;
    }
}
