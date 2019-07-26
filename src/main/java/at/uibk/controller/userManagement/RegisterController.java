package at.uibk.controller.userManagement;

import at.uibk.messages.FacesMessages;
import at.uibk.model.Currency;
import at.uibk.model.mainEntities.Portfolio;
import at.uibk.model.mainEntities.Role;
import at.uibk.model.mainEntities.User;
import at.uibk.navigation.Pages;
import at.uibk.services.beans.PortfolioBean;
import at.uibk.services.beans.UserBean;
import at.uibk.utils.PasswordHash;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

@RequestScoped
@Named
public class RegisterController {

    @Inject
    private User user;
    @EJB
    private UserBean userBean;
    @EJB
    private PortfolioBean portfolioBean;

    public String register(){
        if(userBean.isEmailInUse(user.getEmail())){
            FacesMessages.create(FacesMessage.SEVERITY_ERROR, "Bitte eine andere E-Mail-Adresse wählen!");
            return null;
        }

        this.user.setRole(Role.USER);
        this.user.setPassword(PasswordHash.hash(this.user.getPassword()));
        this.userBean.persist(this.user);

        Portfolio defaultPortfolio = new Portfolio();
        defaultPortfolio.setUser(user);
        defaultPortfolio.setName("MyPortfolio");
        defaultPortfolio.setCurrency(Currency.EUR);
        portfolioBean.persist(defaultPortfolio);

        FacesMessages.create(FacesMessage.SEVERITY_INFO, "Nutzer '" + this.user.getEmail() + "' wurde erstellt!");

        return Pages.login;
    }

    public User getUser() {
        return user;
    }
}
