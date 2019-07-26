package at.uibk.controller.portfolio;

import at.uibk.controller.userManagement.CurrentUser;
import at.uibk.model.mainEntities.Portfolio;
import at.uibk.model.mainEntities.User;
import at.uibk.services.beans.PortfolioBean;
import at.uibk.services.beans.UserBean;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@SessionScoped
@Named
public class PortfolioSelectionController implements Serializable {
    @Inject
    @CurrentUser
    private User currentUser;

    @EJB
    private PortfolioBean portfolioBean;
    @EJB
    private UserBean userBean;

    private long currentPortfolioId;

    @PostConstruct
    public void init() {
        if (currentUser.hasLastPortfolioId()) {
            currentPortfolioId = currentUser.getLastPortfolioId();
            return;
        }

        List<Portfolio> portfolios = portfolioBean.getAll(currentUser.getId());
        Portfolio currentPortfolio = portfolios.get(0);
        this.currentPortfolioId = currentPortfolio.getId();
        persistLastPortfolioId();
    }

    /* producers */

    @Produces
    @Dependent
    @CurrentPortfolio
    public Portfolio getCurrentPortfolio() {
        return portfolioBean.find(currentPortfolioId);
    }

    @Produces
    @Dependent
    @CurrentPortfolioEager
    public Portfolio getCurrentPortfolioEager() {
        return portfolioBean.findEager(currentPortfolioId);
    }


    /* getters and setters*/

    public long getCurrentPortfolioId() {
        return currentPortfolioId;
    }

    public void setCurrentPortfolioId(long portfolioId) {
        this.currentPortfolioId = portfolioId;
        persistLastPortfolioId();
    }


    /* logic */

    private void persistLastPortfolioId() {
        currentUser.setLastPortfolioId(getCurrentPortfolioId());
        userBean.merge(currentUser);
    }

}
