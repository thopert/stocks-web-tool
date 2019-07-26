package at.uibk.controller.portfolio;

import at.uibk.charts.complexChart.ChartDateRange;
import at.uibk.charts.complexChart.ComplexChart;
import at.uibk.charts.complexChart.chartEntries.HoldingPerformanceChartEntry;
import at.uibk.charts.factory.ComplexChartFactory;
import at.uibk.charts.specificCharts.CurrentValueDistributionPieChart;
import at.uibk.charts.specificCharts.HoldingPerformancesBarChart;
import at.uibk.charts.utils.ChartUtils;
import at.uibk.financial_evaluation.HoldingPerformance;
import at.uibk.financial_evaluation.PerformanceEvaluator;
import at.uibk.financial_evaluation.PortfolioPerformance;
import at.uibk.model.mainEntities.Holding;
import at.uibk.model.mainEntities.Portfolio;
import org.primefaces.PF;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.pie.PieChartModel;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ViewScoped
@Named
public class PortfolioInspectionController implements Serializable {
    @Inject
    private PerformanceEvaluator performanceEvaluator;

    @Inject
    @CurrentPortfolioEager
    private Portfolio currentPortfolioEager;

    @Inject
    private ComplexChartFactory complexChartFactory;

    private PortfolioPerformance portfolioPerformance;

    private boolean withClosed = true;

    private ComplexChart<HoldingPerformance> lineChart;

    private ChartDateRange chartDateRange;

    private LineChartModel lineChartModel;

    @PostConstruct
    public void init() {
        initPortfolioPerformance();

        initChartTool();
    }

    /* portfolio performance */

    private void initPortfolioPerformance() {
        portfolioPerformance = performanceEvaluator.getPortfolioPerformance(currentPortfolioEager, withClosed);
    }

    /* chart tool init */

    private void initChartTool() {
        List<Holding> holdings = portfolioPerformance.getHoldingPerformances().stream()
                .sorted(Comparator.comparing(HoldingPerformance::getTotalReturn))
                .map(HoldingPerformance::getHolding)
                .collect(Collectors.toList());

        lineChart = complexChartFactory.getHoldingPerformanceChart();

        for (int i = 0; i < holdings.size(); i++) {
            Holding holding = holdings.get(i);
            HoldingPerformanceChartEntry chartEntry = new HoldingPerformanceChartEntry(holding);
            chartEntry.setTimeSeries(performanceEvaluator.getHoldingPerformanceTimeSeries(holding));
            chartEntry.setColor(ChartUtils.COLORS[i % ChartUtils.COLORS.length]);
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


    /* bar charts */

    public PieChartModel getPieChartModel() {
        CurrentValueDistributionPieChart pieChart = new CurrentValueDistributionPieChart();

        pieChart.setHoldingPerformances(portfolioPerformance.getHoldingPerformances());

        return pieChart.setupChartModel();
    }

    public BarChartModel getBarChartModel() {
        HoldingPerformancesBarChart barChart = new HoldingPerformancesBarChart();

        barChart.setHoldingPerformances(portfolioPerformance.getHoldingPerformances());

        return barChart.setupChartModel();
    }


    /* getter and setter */

    public ComplexChart<HoldingPerformance> getLineChart() {
        return lineChart;
    }

    public LineChartModel getLineChartModel() {
        return lineChartModel;
    }

    public ChartDateRange getChartDateRange() {
        return chartDateRange;
    }

    public Portfolio getPortfolio() {
        return currentPortfolioEager;
    }

    public PortfolioPerformance getPortfolioPerformance() {
        return portfolioPerformance;
    }

    public boolean isWithClosed() {
        return withClosed;
    }

    public void setWithClosed(boolean withClosed) {
        this.withClosed = withClosed;
        init();
    }

    public Portfolio getCurrentPortfolioEager() {
        return currentPortfolioEager;
    }
}
