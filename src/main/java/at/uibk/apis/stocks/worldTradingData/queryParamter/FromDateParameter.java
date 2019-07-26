package at.uibk.apis.stocks.worldTradingData.queryParamter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FromDateParameter implements QueryParameter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private LocalDate fromDate;

    public FromDateParameter(LocalDate fromDate) {
        this.fromDate = fromDate;
    }


    @Override
    public String getKey() {
        return "date_from";
    }

    @Override
    public String getValue() {
        return DATE_TIME_FORMATTER.format(fromDate);
    }
}
