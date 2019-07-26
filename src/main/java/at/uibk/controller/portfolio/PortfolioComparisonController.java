package at.uibk.controller.portfolio;

import at.uibk.charts.annotations.SpecificComplexChart;
import at.uibk.charts.complexChart.ChartDateRange;
import at.uibk.charts.complexChart.ComplexChart;
import at.uibk.charts.complexChart.chartEntries.PortfolioPerformanceChartEntry;
import at.uibk.charts.utils.ChartUtils;
import at.uibk.controller.userManagement.CurrentUser;
import at.uibk.financial_evaluation.PerformanceEvaluator;
import at.uibk.financial_evaluation.PortfolioPerformance;
import at.uibk.model.mainEntities.Portfolio;
import at.uibk.model.mainEntities.User;
import at.uibk.navigation.Pages;
import at.uibk.services.beans.PortfolioBean;
import org.primefaces.PF;
import org.primefaces.model.charts.line.LineChartModel;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Named
@ViewScoped
public class PortfolioComparisonController implements Serializable {
    @EJB
    private PortfolioBean portfolioBean;

    @Inject
    @CurrentUser
    private User currentUser;

    @Inject
    private PerformanceEvaluator performanceEvaluator;

    @Inject @SpecificComplexChart
    private ComplexChart<PortfolioPerformance> lineChart;

    private LineChartModel lineChartModel;

    private ChartDateRange chartDateRange;

    @Inject
    private PortfolioSelectionController portfolioSelectionController;

    private List<Portfolio> portfolios;

    private List<PortfolioPerformance> portfolioPerformances;

    private boolean withClosed = true;

    @PostConstruct
    public void init(){
        portfolios = portfolioBean.getAllEager(currentUser.getId());

        portfolioPerformances = performanceEvaluator.getPortfolioPerformances(portfolios, withClosed);

        initChart();
    }

    private void initChart(){
        lineChart.clearDataSets();
        lineChart.setValueType("return");
        lineChart.setMonthsBack(ComplexChart.SHOW_ALL);

        for (int i = 0; i < portfolios.size(); i++) {
            Portfolio portfolio = portfolios.get(i);

            if(!portfolio.hasHoldings(withClosed))
                continue;

            PortfolioPerformanceChartEntry chartEntry = new PortfolioPerformanceChartEntry(portfolio);

            chartEntry.setColor(ChartUtils.COLORS[i % ChartUtils.COLORS.length]);

            chartEntry.setTimeSeries(performanceEvaluator.getPortfolioPerformanceTimeSeries(portfolio, withClosed));

            lineChart.addDataSet(chartEntry);
        }

        updateLineChartModel();
    }

    public void updateLineChartModel(){
        this.lineChartModel = lineChart.setupChartModel();
    }

    /* handle date range */

    public void createChartDateRange(){
        LocalDate startDate = lineChart.getStartDate();
        LocalDate endDate = lineChart.getEndDate();

        startDate = startDate != null ? startDate : LocalDate.now();
        endDate = endDate != null ? endDate : LocalDate.now();

        chartDateRange = new ChartDateRange(startDate, endDate);
    }

    public void applyChartDateRange(){
        lineChart.setDateRange(chartDateRange);
        updateLineChartModel();
        PF.current().executeScript("PF('dateRangeDLG').hide()");
    }


    /* getters and setters */

    public ComplexChart<PortfolioPerformance> getLineChart() {
        return lineChart;
    }

    public List<PortfolioPerformance> getPortfolioPerformances() {
        return portfolioPerformances;
    }

    public boolean isWithClosed() {
        return withClosed;
    }

    public void setWithClosed(boolean withClosed) {
        this.withClosed = withClosed;
        init();
    }

    public LineChartModel getLineChartModel() {
        return lineChartModel;
    }

    public ChartDateRange getChartDateRange() {
        return chartDateRange;
    }

    /* inspect a specific portfolio */

    public String inspectPortfolio(Portfolio portfolioToInspect){
        portfolioSelectionController.setCurrentPortfolioId(portfolioToInspect.getId());

        return Pages.portfolioInspection + Pages.redirect;
    }

}
