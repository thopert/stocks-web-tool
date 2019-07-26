package at.uibk.controller.userManagement;

import at.uibk.model.mainEntities.User;
import at.uibk.services.beans.SessionBean;
import at.uibk.services.beans.UserBean;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.io.Serializable;

@SessionScoped
@Named
public class SessionInfoController implements Serializable {
    @EJB
    private SessionBean sessionBean;

    private User currentUser;

    @EJB
    private UserBean userBean;

    public String getCurrentUserFullName() {
        return currentUser.getFirstName() + " " + currentUser.getLastName();
    }

    @Produces @CurrentUser
    public User getCurrentUser(){
        return currentUser;
    }

    @Produces @FreshCurrentUser
    public User getFreshCurrentUser(){
        return userBean.find(currentUser.getId());
    }

    @PostConstruct
    public void init(){
        currentUser = sessionBean.getCurrentUser();
    }

}
