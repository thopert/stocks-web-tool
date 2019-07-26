package at.uibk.controller.alarms;

import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeries;
import at.uibk.controller.portfolio.CurrentPortfolio;
import at.uibk.messages.FacesMessages;
import at.uibk.model.mainEntities.Holding;
import at.uibk.model.mainEntities.Portfolio;
import at.uibk.model.mainEntities.StockPriceAlarm;
import at.uibk.model.mainEntities.StockPriceAlarmType;
import at.uibk.services.apis.AlphaVantageService;
import at.uibk.services.beans.HoldingBean;
import at.uibk.services.email.EmailNotifier;
import at.uibk.services.email.StockPriceAlarmBean;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@ViewScoped
@Named
public class StockPriceAlarmController implements Serializable {
    @EJB
    private StockPriceAlarmBean stockPriceAlarmBean;
    @EJB
    private HoldingBean holdingBean;
    @Inject @CurrentPortfolio
    private Portfolio currentPortfolio;
    @Inject
    private AlphaVantageService alphaVantageService;
    @Inject
    private EmailNotifier emailNotifier;

    private List<StockPriceAlarm> stockPriceAlarms;

    private List<Holding> holdings;

    private StockPriceAlarm placeHolder;

    private long selectedHoldingId;

    private boolean isEditMode;

    private StockTimeSeries currentStockTimeSeries;

    @PostConstruct
    public void init() {
        stockPriceAlarms = stockPriceAlarmBean.getAllByPortfolio(currentPortfolio.getId());
        holdings = holdingBean.getAllByPortfolio(currentPortfolio.getId());
    }

    public void persist() {
        if (isEditMode) {
            stockPriceAlarmBean.merge(placeHolder);
            isEditMode = false;
        } else {
            placeHolder.setHolding(getSelectedHolding());
            stockPriceAlarmBean.persist(placeHolder);
            stockPriceAlarms.add(placeHolder);
        }
    }

    public void remove(StockPriceAlarm stockPriceAlarm) {
        stockPriceAlarmBean.remove(stockPriceAlarm);
        stockPriceAlarms.remove(stockPriceAlarm);
    }

    public void sendAlarm(StockPriceAlarm stockPriceAlarm) {
        BigDecimal price = alphaVantageService.getPrice(stockPriceAlarm.getFullyQualifiedSymbol());
        emailNotifier.sendAlarm(stockPriceAlarm, price);
        FacesMessages.create(FacesMessage.SEVERITY_INFO, "Alarm gesendet!");
    }

    private Holding getSelectedHolding() {
        return holdings.stream()
                .filter(holding -> holding.getId() == selectedHoldingId)
                .findFirst().orElseThrow(IllegalStateException::new);
    }


    /* placeholder stuff */

    public void createPlaceHolder() {
        isEditMode = false;

        Holding selectedHolding = getSelectedHolding();

        setupCurrentTimeSeries(selectedHolding);

        BigDecimal currentPrice = currentStockTimeSeries.getPrice();

        placeHolder = new StockPriceAlarm(true, currentPrice, selectedHolding);
    }

    private void setupCurrentTimeSeries(Holding selectedHolding) {
        currentStockTimeSeries = alphaVantageService.getTimeSeriesDaily(selectedHolding.getFullyQualifiedSymbol());
    }

    public void setPlaceHolder(StockPriceAlarm stockPriceAlarm) {
        isEditMode = true;

        this.placeHolder = stockPriceAlarm;

        setupCurrentTimeSeries(stockPriceAlarm.getHolding());
    }

    public StockPriceAlarm getPlaceHolder() {
        return placeHolder;
    }


    public void cleanUp() {
        if(isEditMode) {
            isEditMode = false;
        }

        this.placeHolder = null;
    }


    /* getters and setters */

    public List<StockPriceAlarm> getStockPriceAlarms() {
        return stockPriceAlarms;
    }

    public List<Holding> getHoldings() {
        return holdings;
    }

    public StockTimeSeries getCurrentStockTimeSeries() {
        return currentStockTimeSeries;
    }

    public List<StockPriceAlarmType> getStockPriceAlarmTypes() {
        return Arrays.asList(StockPriceAlarmType.values());
    }

    public long getSelectedHoldingId() {
        return selectedHoldingId;
    }

    public void setSelectedHoldingId(long selectedHoldingId) {
        this.selectedHoldingId = selectedHoldingId;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

}
