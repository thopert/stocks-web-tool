package at.uibk.charts.complexChart;

public enum DefaultChartRange {
    ONE_MONTH(1),
    THREE_MONTHS(3),
    SIX_MONTHS(6),
    ONE_YEAR(12),
    TWO_YEARS(24),
    THREE_YEARS(36),
    FIVE_YEARS(60),
    TOTAL(0);

    DefaultChartRange(int monthsBack) {
        this.monthsBack = monthsBack;
    }

    private int monthsBack;

    public int getMonthsBack() {
        return monthsBack;
    }
}
