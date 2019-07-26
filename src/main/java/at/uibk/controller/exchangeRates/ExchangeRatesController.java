package at.uibk.controller.exchangeRates;

import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRates;
import at.uibk.charts.complexChart.ChartDateRange;
import at.uibk.charts.complexChart.ComplexChart;
import at.uibk.charts.complexChart.chartEntries.ExchangeRatesChartEntry;
import at.uibk.controller.portfolio.CurrentPortfolio;
import at.uibk.model.Currency;
import at.uibk.model.mainEntities.Portfolio;
import at.uibk.services.beans.ExchangeRatesBean;
import org.primefaces.PF;
import org.primefaces.model.charts.line.LineChartModel;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class ExchangeRatesController implements Serializable {

    @EJB
    private ExchangeRatesBean exchangeRatesBean;

    @Inject
    private ComplexChart<ExchangeRates> lineChart;

    private LineChartModel lineChartModel;

    private ChartDateRange chartDateRange;

    private ExchangeRates latestExchangeRates;

    @Inject @CurrentPortfolio
    private Portfolio currentPortfolio;

    @PostConstruct
    public void init(){
        LocalDate startDate = LocalDate.now().minusYears(5);

        NavigableMap<LocalDate, ExchangeRates> ratesTimeSeries =
                exchangeRatesBean.getTailMap(startDate, currentPortfolio.getCurrency());

        latestExchangeRates = ratesTimeSeries.lastEntry().getValue();

        ExchangeRatesChartEntry chartEntry = new ExchangeRatesChartEntry(ratesTimeSeries);

        lineChart.addDataSet(chartEntry);

        updateLineChartModel();
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

    /* getters and setter */
    public ComplexChart<ExchangeRates> getLineChart() {
        return lineChart;
    }

    public LineChartModel getLineChartModel() {
        return lineChartModel;
    }

    public ChartDateRange getChartDateRange() {
        return chartDateRange;
    }

    public ExchangeRates getLatestExchangeRates() {
        return latestExchangeRates ;
    }

    public TreeMap<Currency, BigDecimal> getExchangeRateMap(){
        List<Currency> foreignCurrencies = Arrays.stream(Currency.values())
                .filter(currency -> currency != currentPortfolio.getCurrency())
                .collect(Collectors.toList());

        TreeMap<Currency, BigDecimal> exchangeRateMap = new TreeMap<Currency, BigDecimal>();

        for (Currency foreignCurrency : foreignCurrencies) {
            exchangeRateMap.put(foreignCurrency, latestExchangeRates.getRate(foreignCurrency));
        }

        return exchangeRateMap;
    }

    public Portfolio getCurrentPortfolio() {
        return currentPortfolio;
    }

}
