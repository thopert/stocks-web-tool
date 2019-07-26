package at.uibk.controller.userManagement;

import at.uibk.messages.FacesMessages;
import at.uibk.navigation.Pages;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.security.Principal;

@RequestScoped
@Named
public class AccessController {
    @NotBlank(message = "E-Mail-Adresse leer!")
    private String email;
    @NotBlank (message = "Passwort leer!")
    private String password;

    @Inject
    private HttpServletRequest httpServletRequest;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String login(){
        Principal principal = httpServletRequest.getUserPrincipal();

        if (principal != null) {
            return Pages.portfolioManagement;
        }

        try {
            httpServletRequest.login(email, password);
        } catch (ServletException e) {
            FacesMessages.create(FacesMessage.SEVERITY_ERROR, "Login fehlgeschlagen! Überprüfen Sie Ihre Zugangsdaten.");
            return Pages.login;
        }
        return Pages.portfolioManagement + Pages.redirect;
    }

    public String logout() {
        try {
            httpServletRequest.logout();
            httpServletRequest.getSession(false).invalidate();
        } catch (Exception e) {
            FacesMessages.create(FacesMessage.SEVERITY_ERROR, "Logout fehlgeschlagen! Versuchen Sie es erneut.");
            return null;
        }
        return Pages.index + Pages.redirect;
    }

}
