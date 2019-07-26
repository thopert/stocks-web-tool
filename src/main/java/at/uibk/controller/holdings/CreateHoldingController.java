package at.uibk.controller.holdings;

import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRates;
import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRatesEntry;
import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeriesEntry;
import at.uibk.controller.portfolio.CurrentPortfolio;
import at.uibk.controller.utils.InstrumentSearchBar;
import at.uibk.messages.FacesMessages;
import at.uibk.model.factories.TradeFactory;
import at.uibk.model.mainEntities.*;
import at.uibk.navigation.Pages;
import at.uibk.services.apis.AlphaVantageService;
import at.uibk.services.beans.ExchangeRatesBean;
import at.uibk.services.beans.HoldingBean;
import org.primefaces.event.FlowEvent;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@ViewScoped
@Named
public class CreateHoldingController implements Serializable {
    public static final String SEARCH_TAB_ID = "search_tab";
    public static final String DETAILS_TAB_ID = "details_tab";
    public static final String COMPLETION_TAB_ID = "completion_tab";
    @EJB
    private HoldingBean holdingBean;
    @Inject
    private AlphaVantageService alphaVantageService;
    @EJB
    private ExchangeRatesBean exchangeRatesBean;
    @Inject
    private InstrumentSearchBar instrumentSearchBar;

    @Inject
    @CurrentPortfolio
    private Portfolio currentPortfolio;

    private Trade tradePlaceHolder;

    private Instrument selectedInstrument;

    private StockTimeSeriesEntry priceProposal;

    private ExchangeRates exchangeRateProposal;


    /* proposals for price and exchange rate */

    public void updateProposals() {
        LocalDate date = tradePlaceHolder.getDate();

        priceProposal = alphaVantageService.getFloorClose(selectedInstrument.getFullyQualifiedSymbol(), date);

        exchangeRateProposal = exchangeRatesBean.getFloorEntry(date, currentPortfolio.getCurrency());
    }

    public StockTimeSeriesEntry getPriceProposal() {
        return priceProposal;
    }

    public ExchangeRatesEntry getExchangeRateProposal() {
        if (exchangeRateProposal == null) {
            return null;
        }

        return exchangeRateProposal.getRateEntry(selectedInstrument.getCurrency(), tradePlaceHolder.getExchangeRateDirection());
    }


    /* wizard transition */

    private String handleSearchTab() {
        if (selectedInstrument == null) {
            FacesMessages.create(FacesMessage.SEVERITY_ERROR, "Ein Instrument auswählen!");
            return SEARCH_TAB_ID;
        }

        if (!isUnique()) {
            FacesMessages.create(FacesMessage.SEVERITY_WARN, selectedInstrument.getStockIdentifier() +
                    " bereits vorhanden!");
            selectedInstrument = null;
            return SEARCH_TAB_ID;
        }

        setupTradePlaceHolder();

        try {
            updateProposals();
            return DETAILS_TAB_ID;
        } catch (AlphaVantageException e) {
        }

        FacesMessages.create(FacesMessage.SEVERITY_WARN, selectedInstrument.getStockIdentifier() + " wird von " +
                "AlphaVantage nicht unterstützt!");
        selectedInstrument = null;
        tradePlaceHolder = null;
        return SEARCH_TAB_ID;
    }

    private String handleDetailsTab(String nextStepId) {
        if (nextStepId.equals(SEARCH_TAB_ID)) {
            return SEARCH_TAB_ID;
        }

        persist();

        return COMPLETION_TAB_ID;
    }

    private String handleCompletionTab() {
        resetData();
        return SEARCH_TAB_ID;
    }

    public String handleFlow(FlowEvent event) {
        String nextStepId = event.getNewStep();
        String oldStepId = event.getOldStep();

        if (oldStepId.equals(SEARCH_TAB_ID)) {
            return handleSearchTab();
        }

        if (oldStepId.equals(DETAILS_TAB_ID)) {
            return handleDetailsTab(nextStepId);
        }

        return handleCompletionTab();
    }

    private void setupTradePlaceHolder() {
        Holding holding = new Holding(selectedInstrument, currentPortfolio);

        tradePlaceHolder = TradeFactory.getDefaultTrade(TradeType.BUY, holding);

        holding.addTrade(tradePlaceHolder);
    }

    public void resetData() {
        selectedInstrument = null;
        tradePlaceHolder = null;
        priceProposal = null;
        exchangeRateProposal = null;
    }

    /* persisting holding/trade */

    private boolean isUnique() {
        if (!holdingBean.isUnique(selectedInstrument.getFullyQualifiedSymbol(), currentPortfolio.getId())) {
            return false;
        }
        return true;
    }

    public String persist() {

        holdingBean.persist(tradePlaceHolder.getHolding());

//        FacesMessages.create(FacesMessage.SEVERITY_INFO, "Position " + selectedInstrument.getSymbol() + " eröffnet!");

        return Pages.holdingsManagement;
    }

    /* getters and setters */

    public Instrument getSelectedInstrument() {
        return selectedInstrument;
    }

    public void setSelectedInstrument(Instrument selectedInstrument) {
        this.selectedInstrument = selectedInstrument;
    }

    public Trade getTradePlaceHolder() {
        return tradePlaceHolder;
    }

    public Portfolio getCurrentPortfolio() {
        return currentPortfolio;
    }

    public InstrumentSearchBar getInstrumentSearchBar() {
        return instrumentSearchBar;
    }

    /* listener */

    public void priceProposalListener() {
        this.tradePlaceHolder.setPrice(getPriceProposal().getClose());
    }

    public void exchangeRateListener() {
        BigDecimal selectedRate = getExchangeRateProposal().getRate();
        this.tradePlaceHolder.setExchangeRate(selectedRate);
    }
}
