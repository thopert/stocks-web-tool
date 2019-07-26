package at.uibk.controller.search;

import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockExchange;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeries;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeriesEntry;
import at.uibk.charts.annotations.SpecificComplexChart;
import at.uibk.charts.complexChart.ChartDateRange;
import at.uibk.charts.complexChart.ComplexChart;
import at.uibk.charts.complexChart.chartEntries.StockChartEntry;
import at.uibk.charts.utils.StockIndex;
import at.uibk.messages.FacesMessages;
import at.uibk.model.mainEntities.Instrument;
import at.uibk.services.apis.AlphaVantageService;
import at.uibk.services.beans.InstrumentBean;
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
import java.util.Arrays;
import java.util.List;

@ViewScoped
@Named
public class ChartToolController implements Serializable {
    @Inject
    private InstrumentBean instrumentBean;
    @Inject
    private AlphaVantageService alphaVantageService;
    @Inject
    @SpecificComplexChart
    private ComplexChart<StockTimeSeriesEntry> lineChart;
    @NotNull
    private Instrument selectedInstrument;

    private LineChartModel lineChartModel;

    private StockIndex stockIndex;

    private StockChartEntry chartEntry;

    private ChartDateRange chartDateRange;

    @PostConstruct
    public void init() {
        lineChartModel = lineChart.setupChartModel();
    }

    public void addStockIndex() {
        chartEntry.setId(stockIndex);
        lineChart.setPercentMode(true);
        if (fetchData(stockIndex.getFullyQualifiedSymbol())) {
            FacesMessages.create(FacesMessage.SEVERITY_INFO, stockIndex.getAbbreviation() + " hinzugefügt!");
            chartEntry = new StockChartEntry();
        }
    }

    public void addDataSet() {
        chartEntry.setId(selectedInstrument);
        if (fetchData(selectedInstrument.getFullyQualifiedSymbol())) {
            FacesMessages.create(FacesMessage.SEVERITY_INFO, selectedInstrument.getStockIdentifier() + " hinzugefügt!");
            chartEntry = new StockChartEntry();
            selectedInstrument = null;
        }
    }

    private boolean fetchData(String fullyQualifiedSymbol) {
        StockTimeSeries stockTimeSeries;
        try {
            stockTimeSeries = alphaVantageService.getTimeSeriesDaily(fullyQualifiedSymbol);
        } catch (AlphaVantageException e) {
            FacesMessages.create(FacesMessage.SEVERITY_WARN, e.getMessage());
            return false;
        }

        chartEntry.setTimeSeries(stockTimeSeries.getEntries());
        lineChart.addDataSet(chartEntry);

        updateLineChartModel();

        return true;
    }

    /* autocomplete search method */

    public List<Instrument> completeMethod(String keyWords) {
        return instrumentBean.searchAll(keyWords);
    }

    /* chart entry placeHolder handling */

    public void removeChartEntry(StockChartEntry stockChartEntry){
        lineChart.removeDataSet(stockChartEntry);
        updateLineChartModel();
    }

    public void setChartEntry(StockChartEntry stockChartEntry) {
        chartEntry = stockChartEntry;
    }

    public void createChartEntry() {
        selectedInstrument = null;

        chartEntry = new StockChartEntry();
    }

    public StockChartEntry getChartEntry() {
        return chartEntry;
    }

    public void cleanUp() {
        selectedInstrument = null;
        stockIndex = null;
        chartEntry = null;
    }

    /* update lineChartModel */
    public void updateLineChartModel() {
        lineChartModel = lineChart.setupChartModel();
    }

    /* chart period placeholder handling */
    public void applyChartPeriod() {
        lineChart.setDateRange(chartDateRange);
        lineChartModel = lineChart.setupChartModel();
        PF.current().executeScript("PF('dateRangeDLG').hide()");

//        this.lineChart.setDateRange(chartDateRange);
//        LineChartModel lineChartModel = lineChart.setupChartModel();
//        if(lineChartModel == null){
//            FacesMessages.create(FacesMessage.SEVERITY_ERROR, "Keine Daten im aktuellen Datumsbereich vorhanden!");
//        }else{
//            this.lineChartModel = lineChartModel;
//            PF.current().executeScript("PF('dateRangeDLG').hide()");
//        }
    }

    public void createChartPeriod() {
        LocalDate startDate = lineChart.getStartDate();
        LocalDate endDate = lineChart.getEndDate();

        startDate = startDate != null ? startDate : LocalDate.now();
        endDate = endDate != null ? endDate : LocalDate.now();

        chartDateRange = new ChartDateRange(startDate, endDate);
    }

    public ChartDateRange getChartDateRange() {
        return chartDateRange;
    }

    /* getters and setters */

    public List<StockIndex> getStockIndices() {
        return Arrays.asList(StockIndex.values());
    }

    public List<StockExchange> getStockExchanges() {
        return Arrays.asList(StockExchange.values());
    }

    public StockIndex getStockIndex() {
        return stockIndex;
    }

    public void setStockIndex(StockIndex stockIndex) {
        this.stockIndex = stockIndex;
    }

    public ComplexChart<StockTimeSeriesEntry> getLineChart() {
        return lineChart;
    }

    public LineChartModel getLineChartModel() {
        return lineChartModel;
    }

    public Instrument getSelectedInstrument() {
        return selectedInstrument;
    }

    public void setSelectedInstrument(Instrument selectedInstrument) {
        this.selectedInstrument = selectedInstrument;
    }

}
