package at.uibk.controller.holdings;

import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRates;
import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRatesEntry;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeriesEntry;
import at.uibk.charts.annotations.SpecificComplexChart;
import at.uibk.charts.complexChart.ChartDateRange;
import at.uibk.charts.complexChart.ComplexChart;
import at.uibk.charts.complexChart.chartEntries.HoldingPerformanceChartEntry;
import at.uibk.financial_evaluation.HoldingPerformance;
import at.uibk.financial_evaluation.PerformanceEvaluator;
import at.uibk.messages.FacesMessages;
import at.uibk.model.factories.TradeFactory;
import at.uibk.model.mainEntities.*;
import at.uibk.navigation.Pages;
import at.uibk.services.apis.AlphaVantageService;
import at.uibk.services.beans.ExchangeRatesBean;
import at.uibk.services.beans.HoldingBean;
import org.primefaces.PF;
import org.primefaces.model.charts.line.LineChartModel;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

@SessionScoped
@Named
public class HoldingDetailsController implements Serializable {
    @EJB
    private HoldingBean holdingBean;
    @EJB
    private ExchangeRatesBean exchangeRatesBean;
    @Inject
    private AlphaVantageService alphaVantageService;
    @Inject
    private PerformanceEvaluator performanceEvaluator;
    @Inject
    private TradesAndDividendsManager tradesAndDividendsManager;
    @Inject
    @SpecificComplexChart
    private ComplexChart<HoldingPerformance> lineChart;

    private LineChartModel lineChartModel;

    private ChartDateRange chartDateRange;

    private Portfolio currentPortfolio;

    private Holding selectedHolding;

    private Trade tradePlaceHolder;

    private Dividend dividendPlaceHolder;

    private HoldingDetailsMode mode;

    private StockTimeSeriesEntry priceProposal;
    private ExchangeRates exchangeRatesProposal;

    private List<Dividend> missingDividends;
    private List<Dividend> selectedMissingDividends;

    private List<Trade> missingAdjustments;
    private List<Trade> selectedMissingAdjustments;

    /* initialization */

    public void init(Holding holdingEager) {

        selectedHolding = holdingEager;

        tradesAndDividendsManager.init(holdingEager);

        currentPortfolio  = holdingEager.getPortfolio();

        initChart();
    }

    public void initChart() {
        TreeMap<LocalDate, HoldingPerformance> timeSeries = performanceEvaluator.getHoldingPerformanceTimeSeries(selectedHolding);

        HoldingPerformanceChartEntry chartEntry = new HoldingPerformanceChartEntry(selectedHolding);

        chartEntry.setTimeSeries(timeSeries);

        lineChart.setDataSet(chartEntry);

        lineChart.applyDefaultSettings();

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

    public ChartDateRange getChartDateRange() {
        return chartDateRange;
    }

    public ComplexChart<HoldingPerformance> getLineChart() {
        return lineChart;
    }

    public LineChartModel getLineChartModel() {
        return lineChartModel;
    }

    /* holding stuff */

    public Holding getSelectedHolding() {
        return selectedHolding;
    }

    public HoldingPerformance getHoldingPerformance() {
        return performanceEvaluator.getHoldingPerformance(selectedHolding);
    }

    /* mode handling */

    public HoldingDetailsMode getMode() {
        return mode;
    }

    /* event listeners */

    public void handleDateSelection() {
        updateProposals(mode.isDividend() ? dividendPlaceHolder.getDate() : tradePlaceHolder.getDate());
    }

    public void handlePriceProposalSelection(){
        tradePlaceHolder.setPrice(priceProposal.getClose());
    }

    public void handleExchangeRateSelection(){
        BigDecimal exchangeRate = getExchangeRateProposal().getRate();

        if(mode.isDividend()) {
            dividendPlaceHolder.setExchangeRate(exchangeRate);
        } else{
            tradePlaceHolder.setExchangeRate(exchangeRate);
        }
    }

    /* proposal handling */

    public void updatePriceProposal(LocalDate date) {
        priceProposal = alphaVantageService.getFloorClose(selectedHolding.getFullyQualifiedSymbol(), date);
    }

    public void updateExchangeRateProposal(LocalDate date) {
        // portfolioManagementController.getCurrentPortfolio().getCurrency();

        exchangeRatesProposal = exchangeRatesBean.getFloorEntry(date, currentPortfolio.getCurrency());
    }

    public void updateProposals(LocalDate date) {
        updatePriceProposal(date);
        updateExchangeRateProposal(date);
    }

    public StockTimeSeriesEntry getPriceProposal() {
        return priceProposal;
    }


    public ExchangeRatesEntry getExchangeRateProposal() {
        if (mode == null || exchangeRatesProposal == null)
            return null;

        ExchangeRateDirection exchangeRateDirection = mode.isDividend() ?
                dividendPlaceHolder.getExchangeRateDirection() : tradePlaceHolder.getExchangeRateDirection();

        return exchangeRatesProposal.getRateEntry(selectedHolding.getCurrency(), exchangeRateDirection);
    }

    /* trade placeholder handling */

    public void createTradePlaceHolder(TradeType type) {
        mode = HoldingDetailsMode.TRADE_NEW;

        if (type.isTrade()) {

            updateProposals(LocalDate.now());

            tradePlaceHolder = TradeFactory.getDefaultTrade(TradeType.BUY, selectedHolding);

        } else {
            tradePlaceHolder = TradeFactory.getDefaultAdjustment(selectedHolding);
        }
    }

    public void setTradePlaceHolder(Trade trade) {
        mode = HoldingDetailsMode.TRADE_EDIT;

        this.tradePlaceHolder = new Trade(trade);

        updateProposals(tradePlaceHolder.getDate());
    }

    public Trade getTradePlaceHolder() {
        return tradePlaceHolder;
    }

    /* trade handling */

    public void finishAction() {
        switch (mode) {
            case TRADE_NEW:
                persistTrade();
                break;
            case TRADE_EDIT:
                mergeTrade();
                break;
            case DIVIDEND_NEW:
                tradesAndDividendsManager.persist(dividendPlaceHolder);
                break;
            case DIVIDEND_EDIT:
                tradesAndDividendsManager.merge(dividendPlaceHolder);
                break;
        }
    }

    public void persistTrade() {
        if (!tradesAndDividendsManager.persist(tradePlaceHolder)) {
            FacesMessages.create(FacesMessage.SEVERITY_WARN, "Erstellung nicht möglich!");
        }
    }

    public void mergeTrade() {
        if (!tradesAndDividendsManager.mergeTrade(tradePlaceHolder)) {
            FacesMessages.create(FacesMessage.SEVERITY_WARN, "Update nicht möglich!");
        }
    }

    public String removeTrade(Trade tradeToRemove) {
        if (!tradesAndDividendsManager.removeTrade(tradeToRemove)) {
            FacesMessages.create(FacesMessage.SEVERITY_WARN, "Löschung nicht möglich!");
            return null;
        }

        if (getTrades().size() > 0) {
            return null;
        }

       return removeHolding();
    }

    public String removeHolding(){
        holdingBean.remove(selectedHolding);
//        FacesMessages.create(FacesMessage.SEVERITY_WARN, "Position " + selectedHolding.getFullyQualifiedSymbol() +
//        " wurde entfernt!");

        return Pages.holdingsManagement;
    }

    public List<Trade> getTrades() {
        return tradesAndDividendsManager.getTrades();
    }

    /* reset input data */

    public void resetInputData() {
        tradePlaceHolder = null;
        dividendPlaceHolder = null;
        mode = null;
        priceProposal = null;
        exchangeRatesProposal = null;
    }

    /* dividend placeholder handling */

    public void createDividendPlaceHolder() {
        mode = HoldingDetailsMode.DIVIDEND_NEW;

        updateExchangeRateProposal(LocalDate.now());

        dividendPlaceHolder = new Dividend(selectedHolding);
    }

    public void setDividendPlaceHolder(Dividend dividend) {
        mode = HoldingDetailsMode.DIVIDEND_EDIT;

        this.dividendPlaceHolder = new Dividend(dividend);

        updateExchangeRateProposal(dividendPlaceHolder.getDate());
    }

    public Dividend getDividendPlaceHolder() {
        return dividendPlaceHolder;
    }

    /* dividend handling */

    public void removeDividend(Dividend dividend) {
        tradesAndDividendsManager.remove(dividend);
    }

    public List<Dividend> getDividends() {
        return tradesAndDividendsManager.getDividends();
    }

    /* missing dividends */

    public boolean hasMissingDividends() {
        return tradesAndDividendsManager.hasMissingDividends();
    }

    public void setupMissingDividends() {
        missingDividends = tradesAndDividendsManager.getMissingDividends();
    }

    public List<Dividend> getMissingDividends() {
        return missingDividends;
    }

    public void persistMissingDividends() {
        tradesAndDividendsManager.persistDividends(selectedMissingDividends);
    }

    public List<Dividend> getSelectedMissingDividends() {
        return selectedMissingDividends;
    }

    public void setSelectedMissingDividends(List<Dividend> selectedMissingDividends) {
        this.selectedMissingDividends = selectedMissingDividends;
    }

    public void resetMissingDividends() {
        missingDividends = null;
        selectedMissingDividends = null;
    }

    /* missing adjustments */

    public void setupMissingAdjustments() {
        missingAdjustments = tradesAndDividendsManager.getMissingAdjustments();
    }

    public boolean hasMissingAdjustments() {
        return tradesAndDividendsManager.hasMissingAdjustments();
    }

    public List<Trade> getMissingAdjustments() {
        return missingAdjustments;
    }

    public void persistMissingAdjustments() {
        tradesAndDividendsManager.persistAdjustments(selectedMissingAdjustments);
    }

    public List<Trade> getSelectedMissingAdjustments() {
        return selectedMissingAdjustments;
    }

    public void setSelectedMissingAdjustments(List<Trade> selectedMissingAdjustments) {
        this.selectedMissingAdjustments = selectedMissingAdjustments;
    }

    public void resetMissingAdjustments() {
        missingAdjustments = null;
        selectedMissingAdjustments = null;
    }

}