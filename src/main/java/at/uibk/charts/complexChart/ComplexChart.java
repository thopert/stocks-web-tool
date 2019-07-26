package at.uibk.charts.complexChart;

import at.uibk.charts.complexChart.chartEntries.ChartEntry;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComplexChart<T> implements Serializable {
    private static final int DEFAULT_MONTHS_BACK = 1;
    public static final int SHOW_ALL = 0;

    private enum Mode {
        MONTHS, DATE_RANGE
    }

    private Set<ChartEntry<T>> chartEntries = new HashSet<>();

    /* helper data */

    private List<LocalDate> dates;
    private HashMap<String, List<BigDecimal>> dataPointsMap;
    private int numberOfEntries;
    private LocalDate startDate;
    private LocalDate endDate;

    /* settings */

    private Mode mode;
    private int monthsBack;
    private ChartDateRange dateRange;
    private String valueType;
    private boolean percentMode;

    /* chart type properties */

    private List<String> valueTypes;

    private List<String> percentValueTypes;

    private String valueTypeDefault;

    public ComplexChart(List<String> valueTypes, List<String> percentValueTypes, String valueTypeDefault) {
        this.valueTypes = valueTypes;
        this.percentValueTypes = percentValueTypes;
        this.valueTypeDefault = valueTypeDefault;
        applyDefaultSettings();
    }

    public LineChartModel setupChartModel() {

        LineChartModel lineChartModel = new LineChartModel();

        if (getVisibleChartEntries().isEmpty()) {
            return lineChartModel;
        }

        init();

        ChartData chartData = setupChartData();

        lineChartModel.setData(chartData);

        setupAppearance(lineChartModel);

        cleanUpTemporaryData();

        return lineChartModel;
    }

    private void cleanUpTemporaryData() {
        dates = null;
        dataPointsMap = null;
    }

    private ChartData setupChartData() {
        ChartData chartData = new ChartData();

        for (ChartEntry<T> chartEntry : getVisibleChartEntries()) {

            setupDataSets(chartData, chartEntry);
        }

        List<String> labels = ChartUtils.getLabels(dates);

        chartData.setLabels(labels);

        return chartData;
    }

    private void setupDataSets(ChartData chartData, ChartEntry<T> chartEntry) {
        List<Number> data = setupDataPoints(chartEntry);

        LineChartDataSet stockDataSet = setupTimeSeriesDataSet(data, chartEntry);

        chartData.addChartDataSet(stockDataSet);

        if (chartEntry.isWithPriceLine() && !data.isEmpty()) {
            Number priceLinePoint = data.get(data.size() - 1);

            LineChartDataSet priceLineDataSet = setupPriceLineDataSet(priceLinePoint, chartEntry);

            chartData.addChartDataSet(priceLineDataSet);
        }
    }

    private LineChartDataSet setupTimeSeriesDataSet(List<Number> data, ChartEntry<T> chartEntry) {
        LineChartDataSet lineChartDataSet = new LineChartDataSet();
        lineChartDataSet.setData(data);
        lineChartDataSet.setLabel(chartEntry.getId());
        lineChartDataSet.setFill(false);
        lineChartDataSet.setPointRadius(0);
        lineChartDataSet.setPointHitRadius(0);
        lineChartDataSet.setPointHoverRadius(0);
        lineChartDataSet.setLabel(chartEntry.getId());
        lineChartDataSet.setBorderColor(chartEntry.getHexColor());
        lineChartDataSet.setBorderWidth(2);
        lineChartDataSet.setXaxisID("xAxes");
        return lineChartDataSet;
    }

    private LineChartDataSet setupPriceLineDataSet(Number priceLinePoint, ChartEntry chartEntry) {
        LineChartDataSet lineChartDataSet = new LineChartDataSet();

        List<Number> data = Stream.generate(() -> priceLinePoint)
                .limit(numberOfEntries)
                .collect(Collectors.toList());

        lineChartDataSet.setData(data);
        lineChartDataSet.setFill(false);
        lineChartDataSet.setPointRadius(0);
        lineChartDataSet.setPointHitRadius(0);
        lineChartDataSet.setPointHoverRadius(0);
        lineChartDataSet.setBorderColor(chartEntry.getHexColor());
        lineChartDataSet.setBorderWidth(1);
        lineChartDataSet.setBorderDash(Arrays.asList(10, 5));
        lineChartDataSet.setXaxisID("xAxes");
        return lineChartDataSet;
    }

    public void init() {
        initDateRange();

        LocalDate dateToCheck = startDate; // let loop start at deadline

        dataPointsMap = new HashMap<>();

        numberOfEntries = 0;

        dates = new ArrayList<>(); // build time line

        while (dateToCheck.compareTo(endDate) <= 0) {

            boolean isWeekEnd = dateToCheck.getDayOfWeek() == DayOfWeek.SATURDAY || dateToCheck.getDayOfWeek() == DayOfWeek.SUNDAY;

            if (isWeekEnd) {
                dateToCheck = dateToCheck.plusDays(1);
                continue;
            }

            for (ChartEntry chartEntry : chartEntries) {
                BigDecimal dataPoint = chartEntry.getDataPoint(dateToCheck, valueType);

                dataPointsMap.merge(chartEntry.getId(),
                        new ArrayList<>(Arrays.asList(dataPoint)),
                        (l1, l2) -> {
                            l1.addAll(l2);
                            return l1;
                        });
            }

            dates.add(dateToCheck);

            numberOfEntries++;

            dateToCheck = dateToCheck.plusDays(1);
        }
    }

    private void initDateRange() {
        if (mode == Mode.MONTHS) {
            initMonthsBackDateRange();
        } else {
            initPeriodDateRange();
        }
    }

    private void initPeriodDateRange() {
        LocalDate endDate = getVisibleChartEntries().stream()
                .map(chartEntry -> chartEntry.getEndDate(dateRange.getEndDate()))
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);

        if (endDate != null && endDate.compareTo(dateRange.getStartDate()) >= 0) {
            LocalDate startDate = getVisibleChartEntries().stream()
                    .map(chartEntry -> chartEntry.getStartDate(dateRange.getStartDate()))
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder()).orElse(null);

            if (startDate != null && startDate.compareTo(endDate) <= 0) {
                this.startDate = startDate;
                this.endDate = endDate;
                return;
            }
        }

        this.startDate = dateRange.getStartDate();
        this.endDate = dateRange.getEndDate();
    }

//    private void initPeriodDateRangeOld() {
//        endDate = getVisibleChartEntries().stream()
//                .map(chartEntry -> chartEntry.getEndDate(dateRange.getEndDate()))
//                .filter(Objects::nonNull)
//                .max(Comparator.naturalOrder()).orElse(dateRange.getEndDate());
//
//        startDate = getVisibleChartEntries().stream()
//                .map(chartEntry -> chartEntry.getStartDate(dateRange.getStartDate()))
//                .filter(Objects::nonNull)
//                .min(Comparator.naturalOrder()).orElse(dateRange.getStartDate());
//
//        if(startDate.compareTo(endDate) > 0){
//            startDate = dateRange.getStartDate();
//            endDate = dateRange.getEndDate();
//        }
//    }

    private void initMonthsBackDateRange() {
        this.endDate = getVisibleChartEntries().stream()
                .map(ChartEntry::getEndDate)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(LocalDate.now());

        LocalDate deadLine = endDate.minusMonths(monthsBack);

        boolean isShowAll = monthsBack == SHOW_ALL;

        this.startDate = getVisibleChartEntries().stream()
                .map(chartEntry -> isShowAll ? chartEntry.getStartDate() : chartEntry.getStartDate(deadLine))
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(deadLine);
    }

    private List<Number> setupDataPoints(ChartEntry stockChartEntry) {
        List<BigDecimal> dataPoints = dataPointsMap.get(stockChartEntry.getId());

        // if no dates were checked (only weekends or invalid date range)
        if (dataPoints == null || dataPoints.isEmpty()) {
            return new ArrayList<>();
        }

        if (!percentMode) {
            return new ArrayList<>(dataPoints);
        }

        BigDecimal startPoint = null;
        boolean startingPointReached = false;
        List<Number> resultingDataPoints = new ArrayList<>();

        for (BigDecimal dataPoint : dataPoints) {
            if (dataPoint == null || dataPoint.compareTo(BigDecimal.ZERO) == 0) {
                resultingDataPoints.add(dataPoint);
                continue;
            }

            if (!startingPointReached) {
                startPoint = dataPoint;
                startingPointReached = true;
            }

            resultingDataPoints.add(computePercentDataPoint(startPoint, dataPoint));
        }

        return resultingDataPoints;
    }

    private BigDecimal computePercentDataPoint(BigDecimal startPoint, BigDecimal dataPoint) {
        return dataPoint
                .divide(startPoint, 4, RoundingMode.HALF_EVEN)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));
    }

    private void setupAppearance(LineChartModel lineChartModel) {
        LineChartOptions options = new LineChartOptions();
        options.setSpanGaps(true);
        options.setShowLines(true); // default value


        /* setup legend */
        Legend legend = new Legend();
        legend.setDisplay(false);
        legend.setPosition("bottom");
        options.setLegend(legend);

        /* setup tooltip */
        Tooltip tooltip = new Tooltip();
        tooltip.setEnabled(false);
        tooltip.setIntersect(true);
        tooltip.setMode("point");
        tooltip.setCaretPadding(10);
        tooltip.setCaretSize(10);
        options.setTooltip(tooltip);

        /* setup axes */
        CartesianScales cartesianScales = new CartesianScales();
        CartesianLinearAxes axesX = new CartesianLinearAxes();
        axesX.setId("xAxes");
        cartesianScales.addXAxesData(axesX);

        /* grid Lines */
        AxesGridLines axesGridLines = new AxesGridLines();
        axesGridLines.setDisplay(true);
        axesX.setGridLines(axesGridLines);

        /* labels/ticks */
        CartesianLinearTicks cartesianLinearTicks = new CartesianLinearTicks();
        cartesianLinearTicks.setMinRotation(0);
        cartesianLinearTicks.setMaxRotation(0);
        cartesianLinearTicks.setAutoSkip(false); // display all labels
        axesX.setTicks(cartesianLinearTicks);

        options.setScales(cartesianScales);
        lineChartModel.setOptions(options);
    }

    /* data set handling */

    public void addDataSet(ChartEntry<T> chartEntry) {
        chartEntries.add(chartEntry);
    }

    public void setDataSet(ChartEntry<T> chartEntry) {
        if (!chartEntries.isEmpty())
            chartEntries.clear();

        addDataSet(chartEntry);
    }

    public void clearDataSets() {
        chartEntries.clear();
    }

    public void removeDataSet(ChartEntry<T> chartEntry) {
        chartEntries.remove(chartEntry);

        if (chartEntries.isEmpty()) {
            applyDefaultSettings();
        }
    }

    /* getters and setters */

    public void setPercentMode(boolean percentMode) {
        this.percentMode = percentMode;
    }

    public boolean isPercentMode() {
        return percentMode;
    }

    public Set<ChartEntry<T>> getChartEntries() {
        return chartEntries;
    }

    public List<ChartEntry<T>> getVisibleChartEntries() {
        return chartEntries.stream()
                .filter(ChartEntry::isVisible)
                .collect(Collectors.toList());
    }

    public int getMonthsBack() {
        return monthsBack;
    }

    public void setMonthsBack(int monthsBack) {
        this.mode = Mode.MONTHS;
        this.monthsBack = monthsBack;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public List<String> getValueTypes() {
        if (percentMode)
            return percentValueTypes;

        return valueTypes;
    }

    public void setValueTypes(List<String> valueTypes) {
        this.valueTypes = valueTypes;
    }

    public void setPercentValueTypes(List<String> percentValueTypes) {
        this.percentValueTypes = percentValueTypes;
    }

    public boolean isPercentModeAllowed() {
        return percentValueTypes.contains(valueType);
    }

    public List<DefaultChartRange> getTimeRanges() {
        return Arrays.asList(DefaultChartRange.values());
    }

    public void setDateRange(ChartDateRange dateRange) {
        if (dateRange == null)
            throw new IllegalArgumentException("Period cannot be null!");

        mode = Mode.DATE_RANGE;
        this.dateRange = dateRange;
    }

    /* default settings */

    public void applyDefaultSettings() {
        percentMode = false;
        monthsBack = DEFAULT_MONTHS_BACK;
        valueType = valueTypeDefault;
        mode = Mode.MONTHS;
        startDate = null;
        endDate = null;
    }

}
