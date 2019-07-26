package at.uibk.controller.userManagement;

import at.uibk.messages.FacesMessages;
import at.uibk.model.mainEntities.User;
import at.uibk.navigation.Pages;
import at.uibk.services.beans.UserBean;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

@ViewScoped
@Named
public class UserSettingsController implements Serializable {
    @Inject @FreshCurrentUser
    private User currentUser;

    @EJB
    private UserBean userBean;

    @Inject
    private HttpServletRequest httpServletRequest;

    public User getCurrentUser() {
        return currentUser;
    }

    public String removeUser(){
        userBean.remove(currentUser);

        FacesMessages.create(FacesMessage.SEVERITY_INFO, "Nutzer '" + currentUser.getEmail() + "' wurde entfernt!");

        try {
            httpServletRequest.logout();
        } catch (ServletException e) {
            e.printStackTrace();
        }

        HttpSession session = httpServletRequest.getSession(false);

        if(session != null){
            session.invalidate();
        }

        return Pages.index;
    }
}
