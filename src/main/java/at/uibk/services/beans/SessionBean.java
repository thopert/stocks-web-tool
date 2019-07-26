package at.uibk.services.beans;


import at.uibk.model.mainEntities.User;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import java.io.Serializable;

@Stateless
public class SessionBean implements Serializable {
    @Resource
    private SessionContext sessionContext;
    @EJB
    private UserBean userBean;

    public User getCurrentUser(){
//        refreshCurrentUser();
        return userBean.getUserByEmail(sessionContext.getCallerPrincipal().getName());
    }

    public long getCurrentUserId(){ // Id cannot be changed, so user must not be fresh
//        if (currentUser == null)
//            refreshCurrentUser();
//        return currentUser.getId();
        return userBean.getUserByEmail(sessionContext.getCallerPrincipal().getName()).getId();
    }


//    public void refreshCurrentUser(){
//        String email = this.sessionContext.getCallerPrincipal().getName();
//        currentUser = userBean.getUserByEmail(email);
//    }
}
