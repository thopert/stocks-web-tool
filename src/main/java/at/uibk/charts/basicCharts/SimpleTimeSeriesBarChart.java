package at.uibk.charts.basicCharts;

import at.uibk.charts.utils.ChartUtils;
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
import java.util.*;
import java.util.function.Function;

public class SimpleTimeSeriesBarChart<T> implements Serializable {
    /* some properties */
    private static final int DECIMAL_SCALE = 2;
    private static final int ROUND_HALF_EVEN = BigDecimal.ROUND_HALF_EVEN;

    private NavigableMap<LocalDate, T> timeSeries;

    private Function<T, BigDecimal> valueExtractor;

    private int monthsBack;

    public SimpleTimeSeriesBarChart() {
    }

    public void setTimeSeries(NavigableMap<LocalDate, T> timeSeries) {

        this.timeSeries = timeSeries;
    }

    public void setValueExtractor(Function<T, BigDecimal> valueExtractor) {
        Function<T, BigDecimal> extractorWrapper = t ->
                valueExtractor.apply(t).setScale(DECIMAL_SCALE, ROUND_HALF_EVEN);

        this.valueExtractor = extractorWrapper;
    }

    public void setMonthsBack(int monthsBack) {
        this.monthsBack = monthsBack;
    }

    public BarChartModel setupChartModel() {
        BarChartModel barChartModel = new BarChartModel();

        if (valueExtractor == null)
            return barChartModel;

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

        Iterator<Map.Entry<LocalDate, T>> iterator = timeSeries.descendingMap().entrySet().iterator();

        int monthsGoneBack = 0;
        while(iterator.hasNext() && monthsGoneBack < monthsBack){
            Map.Entry<LocalDate, T> timeSeriesEntry = iterator.next();

            BigDecimal value = valueExtractor.apply(timeSeriesEntry.getValue());

            data.add(value);


            LocalDate date = timeSeriesEntry.getKey();

            labels.add(date.format(ChartUtils.DATE_TIME_MONTH));

            colors.add(value.compareTo(BigDecimal.ZERO) >= 0 ? ChartUtils.COLOR_GREEN : ChartUtils.COLOR_RED);

            monthsGoneBack++;
        }

        Collections.reverse(data);
        Collections.reverse(labels);
        Collections.reverse(colors);

        barChartDataSet.setData(data);
        barChartDataSet.setBackgroundColor(colors);
        barChartDataSet.setXaxisID("xAxis");

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
        cartesianLinearTicks.setMaxRotation(0);
        cartesianLinearTicks.setMinRotation(0);
        linearAxesX.setTicks(cartesianLinearTicks);
    }

}
