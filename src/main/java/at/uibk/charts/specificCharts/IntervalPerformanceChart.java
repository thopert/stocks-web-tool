package at.uibk.charts.specificCharts;

import at.uibk.charts.utils.ChartUtils;
import at.uibk.financial_evaluation.IntervalPerformance;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.NavigableMap;

public class IntervalPerformanceChart implements Serializable {
    private NavigableMap<LocalDate, IntervalPerformance> timeSeries;

    public IntervalPerformanceChart() {
    }

    public void setTimeSeries(NavigableMap<LocalDate, IntervalPerformance> timeSeries) {
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

    private ChartData setupChartData() {
        ChartData chartData = new ChartData();

        BarChartDataSet barChartDataSet= new BarChartDataSet();

        ArrayList<Number> data = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> colors = new ArrayList<>();


        for (Map.Entry<LocalDate, IntervalPerformance> mapEntry : timeSeries.entrySet()) {
            BigDecimal change = mapEntry.getValue().getChange();

            data.add(change);

            labels.add(getLabel(mapEntry.getValue()));

            colors.add(change.compareTo(BigDecimal.ZERO) >= 0 ? ChartUtils.COLOR_GREEN : ChartUtils.COLOR_RED);
        }

        barChartDataSet.setData(data);
        barChartDataSet.setBackgroundColor(colors);
        barChartDataSet.setXaxisID("xAxis");

        chartData.addChartDataSet(barChartDataSet);
        chartData.setLabels(labels);

        return chartData;
    }

    private String getLabel(IntervalPerformance value) {
        LocalDate startDate = value.getFromDate();
        LocalDate endDate = value.getToDate();
        if(startDate.equals(endDate))
            return endDate.format(ChartUtils.DATE_TIME_DAY);

        return startDate.format(ChartUtils.DATE_TIME_DAY) + " - " +
                endDate.format(ChartUtils.DATE_TIME_DAY);

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
        CartesianLinearAxes linearAxesX = new CartesianLinearAxes();
        linearAxesX.setId("xAxis");
        cartesianScales.addXAxesData(linearAxesX);
        options.setScales(cartesianScales);

        /* grid Lines */
        AxesGridLines axesGridLines = new AxesGridLines();
        axesGridLines.setDisplay(true);
        linearAxesX.setGridLines(axesGridLines);

        /* labels/ticks */
        CartesianLinearTicks cartesianLinearTicks = new CartesianLinearTicks();
        cartesianLinearTicks.setAutoSkip(false); // display all labels
        cartesianLinearTicks.setMaxRotation(45);
        cartesianLinearTicks.setMinRotation(45);
        linearAxesX.setTicks(cartesianLinearTicks);
    }

}
