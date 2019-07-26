package at.uibk.services.beans;

import at.uibk.model.mainEntities.Dividend;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class DividendBean {

    @PersistenceContext
    private EntityManager entityManager;

    public void persist(Dividend dividend){
        entityManager.persist(dividend);
    }

    public void remove(Dividend dividend){
        dividend = entityManager.find(Dividend.class, dividend.getId());
        entityManager.remove(dividend);
    }

    public void merge(Dividend dividend){
        entityManager.merge(dividend);
    }

}
