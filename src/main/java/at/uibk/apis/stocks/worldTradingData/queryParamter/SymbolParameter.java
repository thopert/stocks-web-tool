package at.uibk.apis.stocks.worldTradingData.queryParamter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SymbolParameter implements QueryParameter {
    private List<String> symbols;

    public SymbolParameter(List<String> symbols) {
        this.symbols = symbols;
    }


    public SymbolParameter(String... symbols) {
        this.symbols = Arrays.stream(symbols).collect(Collectors.toList());
    }


    @Override
    public String getKey() {
        return "symbol";
    }

    @Override
    public String getValue() {
        return String.join(",", symbols);
    }

}
