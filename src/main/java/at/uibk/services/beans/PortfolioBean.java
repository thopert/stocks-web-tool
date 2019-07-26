package at.uibk.services.beans;

import at.uibk.model.mainEntities.Portfolio;

import javax.ejb.Stateless;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class PortfolioBean {
    @PersistenceContext
    private EntityManager entityManager;

    public void persist(Portfolio portfolio) {
        this.entityManager.persist(portfolio);
    }

    public void merge(Portfolio portfolio) {
        entityManager.merge(portfolio);
    }

    public void remove(Portfolio portfolio) {
        Portfolio toDelete = this.entityManager.find(Portfolio.class, portfolio.getId());
        this.entityManager.remove(toDelete);
    }

    public Portfolio find(long portfolioId) {
        return entityManager.find(Portfolio.class, portfolioId);
    }

    public Portfolio findEager(long portfolioId) {
        TypedQuery<Portfolio> typedQuery = entityManager.createQuery(
                "SELECT p from " + Portfolio.class.getSimpleName() + " p " +
                        "WHERE p.id = :portfolioId", Portfolio.class);

        typedQuery.setParameter("portfolioId", portfolioId);

        EntityGraph entityGraph = entityManager.getEntityGraph("portfolioWithHoldings");

        typedQuery.setHint("javax.persistence.fetchgraph", entityGraph);

        List<Portfolio> result = typedQuery.getResultList();

        if (result != null && !result.isEmpty())
            return result.get(0);

        return null;
    }

    public Portfolio getFirst(long userId) {
        TypedQuery<Portfolio> tq = this.entityManager.createQuery("SELECT p " +
                "FROM " + Portfolio.class.getSimpleName() + " p " +
                "WHERE p.user.id = :userId " +
                "ORDER BY p.name", Portfolio.class);
        tq.setParameter("userId", userId);
        List<Portfolio> results = tq.getResultList();
        return results != null ? results.get(0) : null;
    }

    public long getPortfolioCount(long userId) {
        TypedQuery<Long> tq = this.entityManager.createQuery("SELECT COUNT(p) " +
                "FROM " + Portfolio.class.getSimpleName() + " p " +
                "WHERE p.user.id = :userId ", Long.class);
        tq.setParameter("userId", userId);
        return tq.getSingleResult();
    }

    public List<Portfolio> getAll(long userId) {
        TypedQuery<Portfolio> tq = this.entityManager.createQuery("SELECT p " +
                "FROM " + Portfolio.class.getSimpleName() + " p " +
                "WHERE p.user.id = :userId " +
                "ORDER BY p.name", Portfolio.class);
        tq.setParameter("userId", userId);
        List<Portfolio> results = tq.getResultList();
        return results == null ? new ArrayList<>() : results;
    }


    public List<Portfolio> getAllEager(long userId) {
        return getAll(userId, true);
    }

    private List<Portfolio> getAll(long userId, boolean eager) {
        TypedQuery<Portfolio> typedQuery = this.entityManager.createQuery(
                "SELECT p FROM " + Portfolio.class.getSimpleName() + " p " +
                "WHERE p.user.id = :userId " +
                "ORDER BY p.name", Portfolio.class);

        typedQuery.setParameter("userId", userId);

        if(eager){
            EntityGraph entityGraph = entityManager.getEntityGraph("portfolioWithHoldings");
            typedQuery.setHint("javax.persistence.fetchgraph", entityGraph);
        }

        List<Portfolio> results = typedQuery.getResultList();

        return results == null ? new ArrayList<>() : results;
    }
}
