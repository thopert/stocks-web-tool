package at.uibk.charts.complexChart.chartEntries;

import at.uibk.financial_evaluation.PortfolioPerformance;
import at.uibk.model.mainEntities.Portfolio;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class PortfolioPerformanceChartEntry extends ChartEntry<PortfolioPerformance> {
    public static final List<String> VALUE_TYPES = Arrays.asList("return", "value");
    public static final String DEFAULT_VALUE_TYPE = "return";


    public PortfolioPerformanceChartEntry(Portfolio portfolio) {
        setId(portfolio.getName() + " (" + portfolio.getCurrency() + ")");
    }


    /* application logic */

    @Override
    protected BigDecimal extractValue(PortfolioPerformance holdingPerformance, String valueType) {
        if (holdingPerformance == null || valueType == null)
            return null;

        if (valueType.equals("return"))
            return normalize(holdingPerformance.getTotalReturn());

        return normalize(holdingPerformance.getCurrentValue());
    }

}

