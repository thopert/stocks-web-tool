package at.uibk.charts.specificCharts;

import at.uibk.charts.utils.ChartUtils;
import at.uibk.financial_evaluation.HoldingPerformance;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.optionconfig.legend.LegendLabel;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.pie.PieChartOptions;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CurrentValueDistributionPieChart implements Serializable {
    private List<HoldingPerformance> holdingPerformances;

    public CurrentValueDistributionPieChart() {
    }

    public CurrentValueDistributionPieChart(List<HoldingPerformance> holdingPerformances) {
        this.holdingPerformances = holdingPerformances;
    }

    public void setHoldingPerformances(List<HoldingPerformance> holdingPerformances) {
        this.holdingPerformances = holdingPerformances;
        holdingPerformances.sort(Comparator.comparing(hp -> hp.getTotalReturn()));

    }

    public PieChartModel setupChartModel() {
        PieChartModel pieChartModel = new PieChartModel();

        if (holdingPerformances == null || holdingPerformances.isEmpty())
            return pieChartModel;

        ChartData chartData = setupChartData();

        pieChartModel.setData(chartData);

        setupConfiguration(pieChartModel);

        return pieChartModel;
    }

    private BigDecimal getPercentage(BigDecimal value, BigDecimal total){
        return value.divide(total, 4, RoundingMode.HALF_EVEN)
                .multiply(BigDecimal.valueOf(100));
    }

    private ChartData setupChartData() {
        ChartData chartData = new ChartData();

        PieChartDataSet pieChartDataSet= new PieChartDataSet();

        ArrayList<Number> dataPoints = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> pieSliceColors = new ArrayList<>();

        BigDecimal total = holdingPerformances.stream()
                .map(HoldingPerformance::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (int i = 0; i < holdingPerformances.size(); i++) {
            HoldingPerformance holdingPerformance = holdingPerformances.get(i);

//            BigDecimal currentValue = holdingPerformance.getCurrentValue();

            BigDecimal currentPercentValue = getPercentage(holdingPerformance.getCurrentValue(), total);

            dataPoints.add(currentPercentValue.setScale(ChartUtils.DECIMAL_SCALE, ChartUtils.ROUND_HALF_EVEN));

            labels.add(holdingPerformance.getHolding().getStockIdentifier().toString());

            pieSliceColors.add("#" + ChartUtils.COLORS[i % ChartUtils.COLORS.length]);
        }

        pieChartDataSet.setData(dataPoints);

        pieChartDataSet.setBackgroundColor(pieSliceColors);

        chartData.addChartDataSet(pieChartDataSet);

        chartData.setLabels(labels);

        return chartData;
    }


    private void setupConfiguration(PieChartModel pieChartModel) {
        PieChartOptions options = new PieChartOptions();

        pieChartModel.setOptions(options);

        /* setup legend */
        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("bottom");
        LegendLabel legendLabel = new LegendLabel();
        legendLabel.setFontSize(14);
//        legendLabel.setFontStyle("bold");
        legendLabel.setFontColor("black");
        legend.setLabels(legendLabel);
        options.setLegend(legend);

    }

}
