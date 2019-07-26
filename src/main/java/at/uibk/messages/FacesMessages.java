package at.uibk.messages;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class FacesMessages {


    public static void create(FacesMessage.Severity severity, String summary) {
        create(severity, summary, null, null);
    }

    public static void create(FacesMessage.Severity severity, String summary, String detail) {
        create(severity, summary, detail, null);
    }

    public static void create(FacesMessage.Severity severity, String summary, String detail, String clientId) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage(severity, summary, detail);
        facesContext.addMessage(clientId, facesMessage);
    }
}
