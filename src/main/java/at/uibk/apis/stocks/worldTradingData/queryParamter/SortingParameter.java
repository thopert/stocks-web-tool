package at.uibk.apis.stocks.worldTradingData.queryParamter;

public enum SortingParameter implements QueryParameter {
    NEWEST("newest"),
    OLDEST("oldest"),
    DESC("desc"),
    ASC("asc");

    private String value;

    SortingParameter(String value) {
        this.value = value;
    }

    @Override
    public String getKey() {
        return "sort";
    }

    @Override
    public String getValue() {
        return value;
    }
}
