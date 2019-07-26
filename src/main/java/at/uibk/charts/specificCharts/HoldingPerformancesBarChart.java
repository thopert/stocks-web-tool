package at.uibk.charts.specificCharts;

import at.uibk.charts.utils.ChartUtils;
import at.uibk.financial_evaluation.HoldingPerformance;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.AxesGridLines;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.optionconfig.tooltip.Tooltip;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HoldingPerformancesBarChart implements Serializable {
    private List<HoldingPerformance> holdingPerformances;

    public HoldingPerformancesBarChart() {
    }

    public void setHoldingPerformances(List<HoldingPerformance> holdingPerformances) {
        this.holdingPerformances = holdingPerformances;
    }

    public BarChartModel setupChartModel() {
        BarChartModel barChartModel = new BarChartModel();

        if (holdingPerformances == null || holdingPerformances.isEmpty())
            return barChartModel;

        ChartData chartData = setupChartData();

        barChartModel.setData(chartData);

        setupConfiguration(barChartModel);

        return barChartModel;
    }

    private ChartData setupChartData() {
        ChartData chartData = new ChartData();

        BarChartDataSet barChartDataSet= new BarChartDataSet();

        ArrayList<Number> dataPoints = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> barColors = new ArrayList<>();

        holdingPerformances.sort(Comparator.comparing(HoldingPerformance::getTotalReturn, Comparator.reverseOrder()));

        for (HoldingPerformance holdingPerformance: holdingPerformances) {

            BigDecimal totalReturn = holdingPerformance.getTotalReturn();
            dataPoints.add(totalReturn.setScale(ChartUtils.DECIMAL_SCALE, ChartUtils.ROUND_HALF_EVEN));

            labels.add(holdingPerformance.getHolding().getStockIdentifier().toString());

            barColors.add(totalReturn.compareTo(BigDecimal.ZERO) >= 0 ? ChartUtils.COLOR_GREEN : ChartUtils.COLOR_RED);
        }

        barChartDataSet.setData(dataPoints);
        barChartDataSet.setBackgroundColor(barColors);
        barChartDataSet.setXaxisID("xAxis");
        barChartDataSet.setYaxisID("yAxis");

        chartData.addChartDataSet(barChartDataSet);
        chartData.setLabels(labels);

        return chartData;
    }


    private void setupConfiguration(BarChartModel barChartModel) {
        BarChartOptions options = new BarChartOptions();

        barChartModel.setOptions(options);

        /* setup legend */
        Legend legend = new Legend();
        legend.setDisplay(false);
        options.setLegend(legend);

        /* setup tooltip */
        Tooltip tooltip = new Tooltip();
        tooltip.setIntersect(true);
        tooltip.setMode("point");
        tooltip.setCaretPadding(10);
        tooltip.setCaretSize(10);
        tooltip.setEnabled(true);
        options.setTooltip(tooltip);

        /* setup axes */
        CartesianScales cartesianScales = new CartesianScales();
        options.setScales(cartesianScales);

        CartesianLinearAxes xAxis = new CartesianLinearAxes();
        xAxis.setId("xAxis");
        cartesianScales.addXAxesData(xAxis);

        CartesianLinearAxes yAxis = new CartesianLinearAxes();
        yAxis.setId("yAxis");
        cartesianScales.addYAxesData(yAxis);

        /* grid Lines */
        AxesGridLines axesGridLines = new AxesGridLines();
        axesGridLines.setDisplay(true);
        xAxis.setGridLines(axesGridLines);

        /* labels/ticks */
        CartesianLinearTicks xTicks = new CartesianLinearTicks();
        xTicks.setAutoSkip(false); // display all labels
        xTicks.setMaxRotation(0);
        xTicks.setMinRotation(0);
        xAxis.setTicks(xTicks);

        CartesianLinearTicks yTicks = new CartesianLinearTicks();
        yTicks.setBeginAtZero(true);
        yAxis.setTicks(yTicks);
    }

}
