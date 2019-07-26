package at.uibk.charts.factory;

import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRates;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeriesEntry;
import at.uibk.charts.annotations.SpecificComplexChart;
import at.uibk.charts.complexChart.ComplexChart;
import at.uibk.charts.complexChart.chartEntries.ExchangeRatesChartEntry;
import at.uibk.charts.complexChart.chartEntries.HoldingPerformanceChartEntry;
import at.uibk.charts.complexChart.chartEntries.PortfolioPerformanceChartEntry;
import at.uibk.charts.complexChart.chartEntries.StockChartEntry;
import at.uibk.controller.portfolio.CurrentPortfolio;
import at.uibk.financial_evaluation.HoldingPerformance;
import at.uibk.financial_evaluation.PortfolioPerformance;
import at.uibk.model.Currency;
import at.uibk.model.mainEntities.Portfolio;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class ComplexChartFactory implements Serializable {

    @Inject
    @CurrentPortfolio
    private Portfolio currentPortfolio;

    @Produces
    @SpecificComplexChart
    public ComplexChart<HoldingPerformance> getHoldingPerformanceChart(){
        List<String> valueTypes = HoldingPerformanceChartEntry.VALUE_TYPES;

        List<String> percentValueTypes = valueTypes.stream()
                .filter(valueType -> !valueType.equals("return"))
                .collect(Collectors.toList());

        return new ComplexChart<>(valueTypes, percentValueTypes, HoldingPerformanceChartEntry.DEFAULT_VALUE_TYPE);
    }

    @Produces
    @SpecificComplexChart
    public ComplexChart<PortfolioPerformance> getPortfolioPerformanceChart(){
        List<String> valueTypes = PortfolioPerformanceChartEntry.VALUE_TYPES;

        List<String> percentValueTypes = valueTypes.stream()
                .filter(valueType -> !valueType.equals("return"))
                .collect(Collectors.toList());

        return new ComplexChart<>(valueTypes, percentValueTypes, PortfolioPerformanceChartEntry.DEFAULT_VALUE_TYPE);
    }

    @Produces
    @SpecificComplexChart
    public ComplexChart<StockTimeSeriesEntry> getStockChart(){
        List<String> valueTypes = StockChartEntry.VALUE_TYPES;

        return new ComplexChart<>(valueTypes, valueTypes, StockChartEntry.DEFAULT_VALUE_TYPE);

    }

    @Produces
    public ComplexChart<ExchangeRates> getExchangeRatesChart(){
        Currency portfolioCurrency = currentPortfolio.getCurrency();

        List<String> valueTypes = ExchangeRatesChartEntry.VALUE_TYPES.stream()
                .filter(valueType -> portfolioCurrency != Currency.valueOf(valueType))
                .collect(Collectors.toList());

        return new ComplexChart<>(valueTypes, valueTypes, valueTypes.get(0));
    }
}
