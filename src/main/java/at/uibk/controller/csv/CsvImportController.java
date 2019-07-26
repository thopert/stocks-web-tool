package at.uibk.controller.csv;

import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockExchange;
import at.uibk.controller.portfolio.CurrentPortfolio;
import at.uibk.csv.CSVHeaders;
import at.uibk.csv.CSVImporter;
import at.uibk.csv.CSVImporterException;
import at.uibk.csv.EmptyCSVFileException;
import at.uibk.messages.FacesMessages;
import at.uibk.model.Currency;
import at.uibk.model.factories.TradeFactory;
import at.uibk.model.mainEntities.*;
import at.uibk.services.beans.HoldingBean;
import at.uibk.services.beans.InstrumentBean;
import at.uibk.services.beans.TradeBean;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.UploadedFile;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ViewScoped
@Named
public class CsvImportController implements Serializable {
    public static final String UPLOAD_TAB = "upload";
    public static final String HEADERS_TAB = "headers";
    public static final String TRADES_TAB = "trades";
    public static final String FINISH_TAB = "finish";

    @EJB
    private HoldingBean holdingBean;
    @EJB
    private TradeBean tradeBean;
    @EJB
    private InstrumentBean instrumentBean;
    @Inject
    @CurrentPortfolio
    private Portfolio currentPortfolio;
    @Inject
    private CSVImporter csvImporter;

    private List<Holding> csvHoldings;

    private CSVHeaders csvHeaders;

    private UploadedFile uploadedFile;

    private void resetAll() {
        csvHoldings = null;
        csvHeaders = null;
        uploadedFile = null;
    }

    public String handleFlow(FlowEvent event) {
        String nextStep = event.getNewStep();
        String oldStep = event.getOldStep();

        if (oldStep.equals(UPLOAD_TAB)) {
            return handleUploadTab();
        }

        if (oldStep.equals(HEADERS_TAB)) {
            return handleHeadersTab(nextStep);
        }

        if (oldStep.equals(TRADES_TAB)) {
            return handleTradesTab(nextStep);
        }

        return handleFinishTab();
    }

    private String handleFinishTab() {
        resetAll();
        return UPLOAD_TAB;
    }

    private String handleTradesTab(String nextStep) {
        if (nextStep.equals(HEADERS_TAB))
            return HEADERS_TAB;

        if (!finishImport()) {
            return TRADES_TAB;
        }

        return FINISH_TAB;
    }

    private String handleHeadersTab(String nextStep) {
        if (nextStep.equals(UPLOAD_TAB)) {
            return UPLOAD_TAB;
        }

        if (!importTrades()) {
            return HEADERS_TAB;
        }

        return TRADES_TAB;
    }

    private String handleUploadTab() {
        if (csvHeaders == null) {
            FacesMessages.create(FacesMessage.SEVERITY_WARN, "Bitte eine CSV-Datei hochladen!");
            return UPLOAD_TAB;
        }
        return HEADERS_TAB;
    }

    public void handleFileUpload(FileUploadEvent fileUploadEvent) {
        uploadedFile = fileUploadEvent.getFile();

        if (uploadedFile == null) {
            FacesMessages.create(FacesMessage.SEVERITY_WARN, "Fehler bei Upload!");
            return;
        }

        if (init()) {
            FacesMessages.create(FacesMessage.SEVERITY_INFO, uploadedFile.getFileName() + " hochgeladen!");
            return;
        }

        uploadedFile = null;
    }

    private boolean init() {
        csvHeaders = null;
        csvHoldings = null;
        try {
            csvHeaders = csvImporter.init(uploadedFile);
            return true;
        } catch (EmptyCSVFileException e) {
            FacesMessages.create(FacesMessage.SEVERITY_ERROR, "CSV-Datei ist leer");
        } catch (CSVImporterException e) {
            FacesMessages.create(FacesMessage.SEVERITY_ERROR, "CSV-Datei konnte nicht gelesen werden!");
        }

        return false;
    }

    private boolean importTrades() {
        if (!csvHeaders.obligatoryHeadersSet()) {
            FacesMessages.create(FacesMessage.SEVERITY_WARN, "Pflichtfelder nicht zugeordnet!");
            return false;
        }

        csvHoldings = null;
        try {
            csvHoldings = csvImporter.importTrades(csvHeaders);
            return true;
        } catch (CSVImporterException e) {
            FacesMessages.create(FacesMessage.SEVERITY_WARN, "CSV-Datei fehlerhaft!");
        }

        return false;
    }


    public boolean finishImport() {
        if (!isReadyToPersist()) {
            return false;
        }

        persistImports();

        return true;
    }

    private void persistImports() {
        List<Holding> holdingsToPersist = new ArrayList<>();
        List<Trade> tradesToPersist = new ArrayList<>();

        for (Holding csvHolding : csvHoldings) {
            List<Trade> csvTradesPerHolding = csvHolding.getTrades();

            if (csvTradesPerHolding.isEmpty()) {
                continue;
            }

            tradesToPersist.addAll(csvTradesPerHolding);

            Holding holdingToPersist = holdingBean.getBy(csvHolding.getStockIdentifier(), currentPortfolio.getId());

            if (holdingToPersist == null) {
                Instrument instrument = instrumentBean.findByStockIdentifier(csvHolding.getStockIdentifier());

                holdingToPersist = new Holding(instrument, currentPortfolio);

                holdingsToPersist.add(holdingToPersist);
            }

            for (Trade trade : csvTradesPerHolding) {
                trade.setHolding(holdingToPersist);
            }
        }

        holdingsToPersist.forEach(holdingBean::persist);
        tradesToPersist.forEach(tradeBean::persist);
    }

    private boolean isReadyToPersist() {
        if (!hasTradesToPersist()) {
            return false;
        }

        if (!hasCorrectBrokerageCurrencies()) {
            return false;
        }

        if (!checkInstruments()) {
            return false;
        }

        return true;
    }

    private boolean hasTradesToPersist() {
        boolean hasTrades = csvHoldings.stream()
                .anyMatch(holding -> !holding.getTrades().isEmpty());

        if (!hasTrades) {
            FacesMessages.create(FacesMessage.SEVERITY_WARN, "Keine Käufe/Verkäufe vorhanden!");
            return false;
        }

        return true;
    }

    private boolean hasCorrectBrokerageCurrencies() {
        boolean soundBrokerageCurrency = getAllTrades().stream()
                .allMatch(trade -> trade.getBrokerageCurrencies().contains(trade.getBrokerageCurrency()));

        if (!soundBrokerageCurrency) {
            FacesMessages.create(FacesMessage.SEVERITY_WARN, "Währung ungültig für aktuelle Auswahl ungültig!");
            return false;
        }
        return true;
    }

    private boolean checkInstruments() {
        boolean status = true;
        for (Holding csvHolding : csvHoldings) {
            if (instrumentBean.findByStockIdentifier(csvHolding.getStockIdentifier()) == null) {
                status = false;
                FacesMessages.create(FacesMessage.SEVERITY_WARN,
                        csvHolding.getStockIdentifier() + " ist nicht verfügbar!");
            }
        }
        return status;
    }


    /* getters and setters */

    public Portfolio getCurrentPortfolio() {
        return currentPortfolio;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public List<Holding> getCsvHoldings() {
        return csvHoldings;
    }

    public CSVHeaders getCsvHeaders() {
        return csvHeaders;
    }

    public List<StockExchange> getStockExchanges() {
        return Arrays.stream(StockExchange.values()).collect(Collectors.toList());
    }

    /* helper */

    private List<Trade> getAllTrades() {
        return csvHoldings.stream()
                .flatMap(holding -> holding.getTrades().stream())
                .collect(Collectors.toList());
    }

    /* creation and deletion of trades*/

    public void deleteTrade(Trade trade) {
        for (Holding importedHolding : csvHoldings) {
            List<Trade> trades = importedHolding.getTrades();
            if (trades.remove(trade)) {
                return;
            }
        }
    }

    public void addTrade(Holding holdingForTrade) {
        Trade tradeToAdd = TradeFactory.getDefaultTrade(TradeType.BUY, holdingForTrade);

        long lastId = getAllTrades().stream()
                .map(Trade::getId)
                .max(Comparator.naturalOrder()).orElse(-1L);

        tradeToAdd.setId(++lastId);

        holdingForTrade.addTrade(tradeToAdd);
    }

    public List<Trade> getImportedTrades(){
        return getAllTrades();
    }

    public void handleStockExchangeSelection(long holdingId) {
        Holding holdingToUpdate = csvHoldings.stream()
                .filter(holding -> holding.getId() == holdingId)
                .findAny().orElseThrow(IllegalStateException::new);

        List<Currency> brokerageCurrencies = holdingToUpdate.getBrokerageCurrencies();

        for (Trade trade : holdingToUpdate.getTrades()) {
            if (!brokerageCurrencies.contains(trade.getBrokerageCurrency()))
                trade.setBrokerageCurrency(currentPortfolio.getCurrency());

            if(holdingToUpdate.hasPortfolioCurrency())
                trade.setExchangeRate(BigDecimal.ONE);
        }
    }
}
