package at.uibk.controller.portfolio;


import at.uibk.controller.userManagement.CurrentUser;
import at.uibk.csv.CSVExporter;
import at.uibk.messages.FacesMessages;
import at.uibk.model.Currency;
import at.uibk.model.mainEntities.Portfolio;
import at.uibk.model.mainEntities.User;
import at.uibk.services.beans.PortfolioBean;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Named
@ViewScoped
public class PortfoliosManagementController implements Serializable {
    @EJB
    private PortfolioBean portfolioBean;
    @Inject @CurrentUser
    private User currentUser;
    @Inject
    private PortfolioSelectionController portfolioSelectionController;
    @Inject
    private CSVExporter csvExporter;

    private List<Portfolio> portfolios;

    private Portfolio selectedPortfolio;

    private Portfolio placeHolder;

    @NotNull(message = "Portfolio für den Export auswählen!")
    private Long exportId;

    private boolean editMode;

    @PostConstruct
    public void init(){
        portfolios = portfolioBean.getAllEager(currentUser.getId());

        selectedPortfolio = portfolios.stream()
                .filter(portfolio -> portfolio.getId() == portfolioSelectionController.getCurrentPortfolioId())
                .findAny().orElseThrow(IllegalStateException::new);
    }

    /* selection listener */
    public void onSelectRadio(SelectEvent event) {
        selectedPortfolio = (Portfolio) event.getObject();
        portfolioSelectionController.setCurrentPortfolioId(selectedPortfolio.getId());
    }

    /* clean up */
    public void cleanUp(){
        placeHolder = null;
        editMode = false;
    }

    /* main actions */

    public void completeDialog(){
        if (editMode) {
            portfolioBean.merge(placeHolder);
        } else {
            portfolioBean.persist(placeHolder);
            portfolios.add(placeHolder);
        }
    }

    public void removePortfolio(Portfolio portfolioToRemove){
        if(portfolioToRemove.equals(selectedPortfolio)){
            FacesMessages.create(FacesMessage.SEVERITY_WARN, "Das aktuelle Portfolio kann nicht gelöscht werden!");
            return;
        }

        portfolioBean.remove(portfolioToRemove);
        portfolios.remove(portfolioToRemove);
    }

    /* placeholder handling */

    public void createPlaceHolder(){
        placeHolder = new Portfolio(currentUser, Currency.DEFAULT);
    }

    public void setPlaceHolder(Portfolio placeHolder) {
        editMode = true;
        this.placeHolder = placeHolder;
    }

    /* getters and setters */

    public Portfolio getPlaceHolder() {
        return placeHolder;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public List<Portfolio> getPortfolios() {
        return portfolios;
    }

    public Portfolio getSelectedPortfolio() {
        return selectedPortfolio;
    }

    public void setSelectedPortfolio(Portfolio selectedPortfolio) {
        this.selectedPortfolio = selectedPortfolio;
    }

    public Long getExportId() {
        return exportId;
    }

    public void setExportId(Long exportId) {
        this.exportId = exportId;
    }

    private Portfolio getPortfolioToExport(){
        return portfolios.stream()
                .filter(portfolio -> portfolio.getId() == exportId)
                .findAny().orElseThrow(IllegalStateException::new);
    }

    /* exporter */
    public StreamedContent exportPortfolio() {
        Portfolio portfolioToExport = getPortfolioToExport();

        String csvExport = csvExporter.fromPortfolio(portfolioToExport);

        ByteArrayInputStream csvExportIS =
                new ByteArrayInputStream(csvExport.getBytes(StandardCharsets.UTF_8));

        String fileName = portfolioToExport.getName() + "-" + portfolioToExport.getCurrency() + ".csv";

        StreamedContent streamedContent = new DefaultStreamedContent(csvExportIS, "text/csv", fileName);

        return streamedContent;
    }


    public StreamedContent getExport(Portfolio portfolio) {

        String csvExport = csvExporter.fromPortfolio(portfolio);

        ByteArrayInputStream csvExportIS =
                new ByteArrayInputStream(csvExport.getBytes(StandardCharsets.UTF_8));

        StreamedContent streamedContent = new DefaultStreamedContent(csvExportIS, "text/csv", "export.csv");

        return streamedContent;
    }

    /* helpers */

    public List<Currency> getSelectableCurrencies() {
        return Arrays.asList(Currency.values());
    }
}
