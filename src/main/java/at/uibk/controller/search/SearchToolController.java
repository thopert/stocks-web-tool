package at.uibk.controller.search;

import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeries;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeriesEntry;
import at.uibk.apis.stocks.alphaVantage.transforming.TransformTimeSeries;
import at.uibk.charts.annotations.SpecificComplexChart;
import at.uibk.charts.basicCharts.SimpleTimeSeriesBarChart;
import at.uibk.charts.complexChart.ChartDateRange;
import at.uibk.charts.complexChart.ComplexChart;
import at.uibk.charts.complexChart.chartEntries.StockChartEntry;
import at.uibk.charts.specificCharts.StockPerformanceBarChart;
import at.uibk.controller.utils.InstrumentSearchBar;
import at.uibk.financial_evaluation.StockPerformanceEntry;
import at.uibk.financial_evaluation.StockPerformanceEvaluator;
import at.uibk.messages.FacesMessages;
import at.uibk.model.mainEntities.Instrument;
import at.uibk.services.apis.AlphaVantageService;
import org.primefaces.PF;
import org.primefaces.model.charts.line.LineChartModel;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

@ViewScoped
@Named
public class SearchToolController implements Serializable {
    @Inject
    private AlphaVantageService alphaVantageService;

    @Inject
    private InstrumentSearchBar instrumentSearchBar;

    @Inject @SpecificComplexChart
    private ComplexChart<StockTimeSeriesEntry> lineChart;

    private LineChartModel lineChartModel;

    private ChartDateRange chartDateRange;

    @Inject
    private SimpleTimeSeriesBarChart<StockTimeSeriesEntry> barChart;
    @Inject
    private StockPerformanceEvaluator stockPerformanceEvaluator;
    @Inject
    private StockPerformanceBarChart performanceChart;

    @NotNull(message = "Eingabefeld darf nicht leer sein!")
    private Instrument selectedInstrument;

    private NavigableMap<Integer, StockPerformanceEntry> performanceMap = new TreeMap<>();

    private StockTimeSeries stockTimeSeries;

    @PostConstruct
    public void init(){
        barChart.setValueExtractor(StockTimeSeriesEntry::getChange);
        barChart.setMonthsBack(12);
    }

    public void fetchStockData() {
        try {
            this.stockTimeSeries = alphaVantageService.getTimeSeriesDaily(selectedInstrument.getFullyQualifiedSymbol());
        } catch (AlphaVantageException e) {
            this.stockTimeSeries = null;
            FacesMessages.create(FacesMessage.SEVERITY_WARN, "Gewähltes Wertpapier wird nicht unterstützt!");
            return;
        }

        lineChart.applyDefaultSettings();

        lineChart.setDataSet(new StockChartEntry(selectedInstrument, stockTimeSeries));

        updateLineChartModel();

        barChart.setTimeSeries(TransformTimeSeries.fromDailyToMonthlyTimeSeries(stockTimeSeries).getEntries());

        stockPerformanceEvaluator.setTimeSeries(stockTimeSeries);

        performanceMap = stockPerformanceEvaluator.getPerformanceMap();

        performanceChart.setTimeSeries(performanceMap);
    }


    public void updateLineChartModel() {
        this.lineChartModel = lineChart.setupChartModel();
    }

    /* handle date range */

    public void createChartDateRange() {
        LocalDate startDate = lineChart.getStartDate();
        LocalDate endDate = lineChart.getEndDate();

        startDate = startDate != null ? startDate : LocalDate.now();
        endDate = endDate != null ? endDate : LocalDate.now();

        chartDateRange = new ChartDateRange(startDate, endDate);
    }

    public void applyChartDateRange() {
        lineChart.setDateRange(chartDateRange);
        updateLineChartModel();
        PF.current().executeScript("PF('dateRangeDLG').hide()");
    }

    /* getter and setter */

    public ComplexChart<StockTimeSeriesEntry> getLineChart() {
        return lineChart;
    }

    public LineChartModel getLineChartModel() {
        return lineChartModel;
    }

    public ChartDateRange getChartDateRange() {
        return chartDateRange;
    }

    public InstrumentSearchBar getInstrumentSearchBar() {
        return instrumentSearchBar;
    }

    public Instrument getSelectedInstrument() {
        return selectedInstrument;
    }

    public void setSelectedInstrument(Instrument selectedInstrument) {
        this.selectedInstrument = selectedInstrument;
    }

    public SimpleTimeSeriesBarChart<StockTimeSeriesEntry> getBarChart() {
        return barChart;
    }

    public StockPerformanceBarChart getPerformanceChart() {
        return performanceChart;
    }

    public StockTimeSeries getStockTimeSeries() {
        return stockTimeSeries;
    }

    public Collection<StockPerformanceEntry> getPerformanceMap(){
        return performanceMap.values();
    }

}
