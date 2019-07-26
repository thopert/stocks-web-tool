package at.uibk.charts.complexChart.chartEntries;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Objects;

public abstract class ChartEntry<T> {
    protected static final String DEFAULT_COLOR = "2c46eb"; // blue
    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_EVEN;

    private String id;
    private String color;
    private boolean withPriceLine;
    private boolean isVisible;
    private NavigableMap<LocalDate, T> timeSeries;
    private int scale;

    public ChartEntry() {
        applyDefaultSettings();
    }

    protected abstract BigDecimal extractValue(T timeSeriesEntry, String valueType);

    public void applyDefaultSettings(){
        setColor(DEFAULT_COLOR);
        setWithPriceLine(true);
        setVisible(true);
        setScale(DEFAULT_SCALE);
    }

    public LocalDate getEndDate(){
        return timeSeries.lastKey();
    }

    public LocalDate getEndDate(LocalDate date){
        return timeSeries.floorKey(date);
    }

    public LocalDate getStartDate() {
        return timeSeries.firstKey();
    }

    public LocalDate getStartDate(LocalDate date){
        return timeSeries.ceilingKey(date);
    }

    public  BigDecimal getDataPoint(LocalDate date, String valueType){
        T timeSeriesEntry = timeSeries.get(date);

        if(timeSeriesEntry == null)
            return null;

        return extractValue(timeSeriesEntry, valueType);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getHexColor() {
        return "#" + color;
    }

    public boolean isWithPriceLine() {
        return withPriceLine;
    }

    public void setWithPriceLine(boolean withPriceLine) {
        this.withPriceLine = withPriceLine;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public NavigableMap<LocalDate, T> getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(NavigableMap<LocalDate, T> timeSeries) {
        this.timeSeries = timeSeries;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    /* helper */

    protected BigDecimal normalize(BigDecimal dataPoint){
        return dataPoint.setScale(scale, DEFAULT_ROUNDING_MODE);
    }

    /* big three*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChartEntry that = (ChartEntry) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
