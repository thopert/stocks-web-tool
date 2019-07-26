package at.uibk.charts.complexChart;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

public class ChartDateRange {

    @PastOrPresent(message = "Datum darf nicht in der Zukunft sein!")
    @NotNull(message = "Datum darf nicht leer sein!")
    private LocalDate startDate;
    @PastOrPresent(message = "Datum darf nicht in der Zukunft sein!")
    @NotNull(message = "Datum darf nicht leer sein!")
    private LocalDate endDate;

    public ChartDateRange() {
    }

    public ChartDateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
