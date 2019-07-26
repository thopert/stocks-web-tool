package at.uibk.charts.complexChart.chartEntries;

import at.uibk.financial_evaluation.HoldingPerformance;
import at.uibk.model.mainEntities.Holding;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class HoldingPerformanceChartEntry extends ChartEntry<HoldingPerformance> {
    public static final List<String> VALUE_TYPES = Arrays.asList("return", "value", "price");
    public static final String DEFAULT_VALUE_TYPE = "return";

    public HoldingPerformanceChartEntry(Holding holding) {
        setId(holding.getStockIdentifier().toString());
    }

    /* application logic */

    @Override
    protected BigDecimal extractValue(HoldingPerformance holdingPerformance, String valueType) {
        if (holdingPerformance == null || valueType == null)
            return null;

        if (valueType.equals("return"))
            return normalize(holdingPerformance.getTotalReturn());

        if (valueType.equals("value"))
            return normalize(holdingPerformance.getCurrentValue());

        return normalize(holdingPerformance.getPrice());
    }

}

