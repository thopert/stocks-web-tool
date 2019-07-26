package at.uibk.controller.holdings;

import at.uibk.charts.annotations.SpecificComplexChart;
import at.uibk.charts.complexChart.ChartDateRange;
import at.uibk.charts.complexChart.ComplexChart;
import at.uibk.charts.complexChart.chartEntries.PortfolioPerformanceChartEntry;
import at.uibk.controller.portfolio.CurrentPortfolioEager;
import at.uibk.financial_evaluation.PerformanceEvaluator;
import at.uibk.financial_evaluation.PortfolioPerformance;
import at.uibk.messages.FacesMessages;
import at.uibk.model.mainEntities.Holding;
import at.uibk.model.mainEntities.Portfolio;
import at.uibk.navigation.Pages;
import at.uibk.services.beans.HoldingBean;
import org.primefaces.PF;
import org.primefaces.model.charts.line.LineChartModel;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.TreeMap;

@ViewScoped
@Named
public class HoldingsManagementController implements Serializable {
    @EJB
    private HoldingBean holdingBean;

    @Inject
    private HoldingDetailsController holdingDetailsController;

    @Inject
    private PerformanceEvaluator performanceEvaluator;

    @Inject
    @SpecificComplexChart
    private ComplexChart<PortfolioPerformance> lineChart;

    private LineChartModel lineChartModel;

    private ChartDateRange chartDateRange;

    @Inject
    @CurrentPortfolioEager
    private Portfolio currentPortfolioEager;

    private PortfolioPerformance portfolioPerformance;

    private boolean withClosed = true;

    @PostConstruct
    public void init() {
        initPortfolioPerformance();

        initChart();
    }

    public String initHoldingDetails(Holding holding) {
        holdingDetailsController.init(holding);
        return Pages.holdingDetails;
    }

    public void initPortfolioPerformance() {
        portfolioPerformance = performanceEvaluator.getPortfolioPerformance(currentPortfolioEager, withClosed);
    }

    private void initChart() {
        if (!currentPortfolioEager.hasHoldings(withClosed)) {
            lineChart.clearDataSets();
        } else {
            PortfolioPerformanceChartEntry chartEntry = new PortfolioPerformanceChartEntry(currentPortfolioEager);
            chartEntry.setTimeSeries(getPortfolioPerformanceTimeSeries());
            lineChart.setDataSet(chartEntry);
        }

        updateLineChartModel();
    }

    public void updateLineChartModel() {
        this.lineChartModel = lineChart.setupChartModel();
    }


    private TreeMap<LocalDate, PortfolioPerformance> getPortfolioPerformanceTimeSeries() {
        return performanceEvaluator.getPortfolioPerformanceTimeSeries(currentPortfolioEager, withClosed);
    }

    public void removeHolding(Holding holding) {
        holdingBean.remove(holding);

        currentPortfolioEager.remove(holding);

        init();

        FacesMessages.create(FacesMessage.SEVERITY_WARN, "Position " + holding.getFullyQualifiedSymbol() + " entfernt!");
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

    public LineChartModel getLineChartModel() {
        return lineChartModel;
    }

    public ChartDateRange getChartDateRange() {
        return chartDateRange;
    }

    public Portfolio getCurrentPortfolioEager() {
        return currentPortfolioEager;
    }

    public PortfolioPerformance getPortfolioPerformance() {
        return portfolioPerformance;
    }

    public ComplexChart<PortfolioPerformance> getLineChart() {
        return lineChart;
    }

    public boolean isWithClosed() {
        return withClosed;
    }

    public void setWithClosed(boolean withClosed) {
        this.withClosed = withClosed;
        init();
    }
}
