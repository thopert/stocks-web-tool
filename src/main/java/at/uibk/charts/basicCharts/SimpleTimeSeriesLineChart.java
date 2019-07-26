package at.uibk.charts.basicCharts;

import at.uibk.charts.utils.ChartUtils;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.AxesGridLines;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.optionconfig.tooltip.Tooltip;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleTimeSeriesLineChart<T> implements Serializable {
    public static final int SHOW_ALL_MONTHS = 0;

    private enum TimeMode {DATE, MONTHS_BACK}

    private TreeMap<LocalDate, T> timeSeries;

    private NavigableMap<LocalDate, T> currentTimeSeries;

    private Function<T, BigDecimal> dataPointExtractor;

    private boolean percentMode;

    private boolean withPriceLine;

    private TimeMode timeMode;

    private int monthsBack;

    private LocalDate deadLine;

    public SimpleTimeSeriesLineChart() {
        applyDefaultSettings();
    }

    public void applyDefaultSettings() {
        this.timeMode = TimeMode.MONTHS_BACK;
        this.monthsBack = SHOW_ALL_MONTHS;
        this.percentMode = false;
        this.withPriceLine = true;
        this.deadLine = null;
    }

    public void setTimeSeries(Map<LocalDate, T> timeSeries) {
        this.timeSeries = new TreeMap<>(timeSeries);

        if (this.timeSeries == null || this.timeSeries.isEmpty()) {
            currentTimeSeries = this.timeSeries;
            return;
        }

        if (timeMode == TimeMode.MONTHS_BACK) {
            if (monthsBack == SHOW_ALL_MONTHS) {
                this.currentTimeSeries = this.timeSeries;
            } else {
                this.currentTimeSeries = this.timeSeries.tailMap(getDateForMonthsBack(), true);
            }
            return;
        }

        if (deadLine == null) {
            this.currentTimeSeries = this.timeSeries;
            return;
        }

        this.currentTimeSeries = this.timeSeries.tailMap(deadLine, true);
    }

    public void setMonthsBack(int monthsBack) {
        this.monthsBack = monthsBack;

        this.timeMode = TimeMode.MONTHS_BACK;

        if (monthsBack == SHOW_ALL_MONTHS) {
            currentTimeSeries = timeSeries;
            return;
        }

        if (timeSeries != null && !timeSeries.isEmpty()) {
            currentTimeSeries = timeSeries.tailMap(getDateForMonthsBack(), true);
        }
    }

    public void setDeadLine(LocalDate date) {
        deadLine = date;

        this.timeMode = TimeMode.DATE;

        if (deadLine == null) {
            currentTimeSeries = timeSeries;
            return;
        }

        currentTimeSeries = timeSeries.tailMap(deadLine, true);
    }


    public void setDataPointExtractor(Function<T, BigDecimal> dataPointExtractor) {
        this.dataPointExtractor = t ->
                dataPointExtractor.apply(t).setScale(ChartUtils.DECIMAL_SCALE, ChartUtils.ROUND_HALF_EVEN);
    }

    private LocalDate getDateForMonthsBack() {

        return timeSeries.lastKey().minusMonths(monthsBack);
    }

    public LineChartModel setupChartModel() {
        LineChartModel lineChartModel = new LineChartModel();

        if (dataPointExtractor == null)
            return lineChartModel;

        if (currentTimeSeries == null || currentTimeSeries.isEmpty())
            return lineChartModel;

        ChartData chartData = setupChartData();

        lineChartModel.setData(chartData);

        setupConfiguration(lineChartModel);

        return lineChartModel;
    }

    private ChartData setupChartData() {
        ChartData chartData = new ChartData();

        LineChartDataSet lineChartDataSet = setupLineChartDataSet();

        chartData.addChartDataSet(lineChartDataSet);

        if (withPriceLine) {
            chartData.addChartDataSet(setupPriceLineDataSet());
        }

        List<String> labels = getLabels();

        chartData.setLabels(labels);

        return chartData;
    }

    private LineChartDataSet setupLineChartDataSet() {
        LineChartDataSet lineChartDataSet = new LineChartDataSet();
        List<Number> values = getDataPoints();
        lineChartDataSet.setData(values);
        lineChartDataSet.setFill(false);
        lineChartDataSet.setPointRadius(0);
        lineChartDataSet.setPointHitRadius(0);
        lineChartDataSet.setPointHoverRadius(0);
        lineChartDataSet.setBorderColor(ChartUtils.DEFAULT_COLOR);
        lineChartDataSet.setBorderWidth(2);
        lineChartDataSet.setXaxisID("xAxis");
        return lineChartDataSet;
    }

    private List<Number> getDataPoints() {
        BigDecimal firstDataPoint = dataPointExtractor.apply(currentTimeSeries.firstEntry().getValue());

        return currentTimeSeries.values().stream()
                .map(timeSeriesEntry -> computeDataPoint(timeSeriesEntry, firstDataPoint))
                .collect(Collectors.toList());
    }

    private BigDecimal computeDataPoint(T timeSeriesValue, BigDecimal firstDataPoint) {
        BigDecimal dataPoint = dataPointExtractor.apply(timeSeriesValue);

        if (!percentMode) {
            return dataPoint;
        }

        return dataPoint
                .divide(firstDataPoint, 4, RoundingMode.HALF_EVEN)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));
    }

    private LineChartDataSet setupPriceLineDataSet() {
        LineChartDataSet lineChartDataSet = new LineChartDataSet();
        List<Number> data = getPriceLineDataPoints();
        lineChartDataSet.setData(data);
        lineChartDataSet.setFill(false);
        lineChartDataSet.setPointRadius(0);
        lineChartDataSet.setPointHitRadius(0);
        lineChartDataSet.setPointHoverRadius(0);
        lineChartDataSet.setBorderColor(ChartUtils.DEFAULT_COLOR);
        lineChartDataSet.setBorderWidth(1);
        lineChartDataSet.setBorderDash(Arrays.asList(10, 5));
        lineChartDataSet.setXaxisID("xAxis");
        return lineChartDataSet;
    }

    private List<Number> getPriceLineDataPoints() {
        T firstDataPoint = currentTimeSeries.firstEntry().getValue();
        T lastDataPoint = currentTimeSeries.lastEntry().getValue();

        BigDecimal priceLinePoint = computeDataPoint(lastDataPoint, dataPointExtractor.apply(firstDataPoint));

        return Stream.generate(() -> priceLinePoint)
                .limit(currentTimeSeries.size())
                .collect(Collectors.toList());
    }

    private List<String> getLabels() {
        return ChartUtils.getLabels(currentTimeSeries.keySet());
    }

    private void setupConfiguration(LineChartModel lineChartModel) {
        LineChartOptions options = new LineChartOptions();

        lineChartModel.setOptions(options);

        /* setup legend */
        Legend legend = new Legend();
        legend.setDisplay(false);
        legend.setPosition("bottom");
        options.setLegend(legend);

        /* setup tooltip */
        Tooltip tooltip = new Tooltip();
        tooltip.setIntersect(true);
        tooltip.setMode("point");
        tooltip.setCaretPadding(10);
        tooltip.setCaretSize(10);
        tooltip.setEnabled(false);
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

    public LocalDate getStartDate() {
        if (currentTimeSeries != null && !currentTimeSeries.isEmpty())
            return currentTimeSeries.firstKey();

        return null;
    }

    public LocalDate getEndDate() {
        if (currentTimeSeries != null && !currentTimeSeries.isEmpty())
            return currentTimeSeries.lastKey();

        return null;
    }

    public boolean isPercentMode() {
        return percentMode;
    }

    public void setPercentMode(boolean percentMode) {
        this.percentMode = percentMode;
    }

    public boolean isWithPriceLine() {
        return withPriceLine;
    }

    public void setWithPriceLine(boolean withPriceLine) {
        this.withPriceLine = withPriceLine;
    }

    public int getMonthsBack() {
        return monthsBack;
    }
}
