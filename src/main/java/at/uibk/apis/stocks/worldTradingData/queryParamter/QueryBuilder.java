package at.uibk.apis.stocks.worldTradingData.queryParamter;

import java.io.Serializable;

public class QueryBuilder implements Serializable {
    private StringBuilder stringBuilder;

    public QueryBuilder() {
        this.stringBuilder = new StringBuilder();
    }

    public QueryBuilder append(QueryParameter queryParameter) {
        if(queryParameter != null) {
            this.append(queryParameter.getKey(), queryParameter.getValue());
        }

        return this;
    }

    private void append(String key, String value) {
        this.stringBuilder.append("&" + key + "=" + value);
    }

    @Override
    public String toString() {
        return this.stringBuilder.toString();
    }

}
