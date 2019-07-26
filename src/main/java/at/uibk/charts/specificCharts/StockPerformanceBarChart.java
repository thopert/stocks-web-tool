package at.uibk.charts.specificCharts;

import at.uibk.charts.utils.ChartUtils;
import at.uibk.financial_evaluation.StockPerformanceEntry;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.AxesGridLines;
import org.primefaces.model.charts.axes.cartesian.CartesianScaleLabel;
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
import java.util.Map;
import java.util.NavigableMap;

public class StockPerformanceBarChart implements Serializable {

    private NavigableMap<Integer, StockPerformanceEntry> timeSeries;

    public StockPerformanceBarChart() {
    }

    public void setTimeSeries(NavigableMap<Integer, StockPerformanceEntry> timeSeries) {
        this.timeSeries = timeSeries;
    }

    public BarChartModel setupChartModel() {
        BarChartModel barChartModel = new BarChartModel();

        if (timeSeries == null || timeSeries.isEmpty())
            return barChartModel;

        ChartData chartData = setupChartData();

        barChartModel.setData(chartData);

        setupConfiguration(barChartModel);

        return barChartModel;
    }

    private BigDecimal getValue(StockPerformanceEntry stockPerformanceEntry){
        return stockPerformanceEntry.getConvertedPercentChange()
                .setScale(ChartUtils.DECIMAL_SCALE, ChartUtils.ROUND_HALF_EVEN);
    }

    private ChartData setupChartData() {
        ChartData chartData = new ChartData();

        BarChartDataSet barChartDataSet= new BarChartDataSet();

        ArrayList<Number> data = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> colors = new ArrayList<>();

        for (Map.Entry<Integer, StockPerformanceEntry> timeSeriesEntry : timeSeries.descendingMap().entrySet()) {
            BigDecimal value = getValue(timeSeriesEntry.getValue());

            data.add(value);

            labels.add(timeSeriesEntry.getValue().getLabel());

            colors.add(value.compareTo(BigDecimal.ZERO) >= 0 ? ChartUtils.COLOR_GREEN : ChartUtils.COLOR_RED);
        }

        barChartDataSet.setData(data);
        barChartDataSet.setBackgroundColor(colors);
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
        CartesianLinearAxes axisX = new CartesianLinearAxes();
        axisX.setId("xAxis");
        cartesianScales.addXAxesData(axisX);

        CartesianLinearAxes axisY = new CartesianLinearAxes();
        axisY.setId("yAxis");
        CartesianScaleLabel cartesianScaleLabel = new CartesianScaleLabel();
        cartesianScaleLabel.setLabelString("%");
        cartesianScaleLabel.setFontSize(18);
        cartesianScaleLabel.setDisplay(true);
        axisY.setScaleLabel(cartesianScaleLabel);
        cartesianScales.addYAxesData(axisY);

        options.setScales(cartesianScales);

        /* grid Lines */
        AxesGridLines axesGridLines = new AxesGridLines();
        axesGridLines.setDisplay(true);
        axisX.setGridLines(axesGridLines);

        /* labels/ticks */
        CartesianLinearTicks cartesianLinearTicks = new CartesianLinearTicks();
        cartesianLinearTicks.setAutoSkip(false); // display all labels
        cartesianLinearTicks.setMaxRotation(0);
        cartesianLinearTicks.setMinRotation(0);
        axisX.setTicks(cartesianLinearTicks);
    }

}
