package at.uibk.apis.stocks.alphaVantage.parameter;

import java.io.Serializable;

public class Keywords implements ApiParameter, Serializable {
    private String value;

    public Keywords(String value){
        this.value = value;
    }

    @Override
    public String getKey() {
        return "keywords";
    }

    @Override
    public String getValue() {
        return value;
    }
}
