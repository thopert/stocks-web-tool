package at.uibk.services.beans;

import at.uibk.model.mainEntities.Trade;
import at.uibk.utils.TradeUtils;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class TradeBean {
    @PersistenceContext
    private EntityManager entityManager;

    public Trade find(long tradeId) {
        return entityManager.find(Trade.class, tradeId);
    }

    public void persistNoChecks(Trade tradeToPersist) {
        entityManager.persist(tradeToPersist);
    }

    public boolean persist(Trade tradeToPersist) {
        if (!tradeToPersist.isReduction()) {
            entityManager.persist(tradeToPersist);
            return true;
        }

        List<Trade> currentTrades = getByHoldingSortedByDate(tradeToPersist.getHolding().getId());

        if (TradeUtils.isPersistCorrect(tradeToPersist, currentTrades)) {
            entityManager.persist(tradeToPersist);
            return true;
        }

        return false;
    }

    public void mergeNoChecks(Trade tradeToMerge) {
        entityManager.merge(tradeToMerge);
    }

    public boolean merge(Trade tradeToMerge) {
        if (!tradeToMerge.isReduction()) {
            entityManager.merge(tradeToMerge);
            return true;
        }

        List<Trade> currentTrades = getByHoldingSortedByDate(tradeToMerge.getHolding().getId());

        if (TradeUtils.isMergeCorrect(tradeToMerge, currentTrades)) {
            entityManager.merge(tradeToMerge);
            return true;
        }

        return false;
    }

    public void removeNoChecks(Trade tradeToRemove) {
        tradeToRemove = entityManager.find(Trade.class, tradeToRemove.getId());
        entityManager.remove(tradeToRemove);
    }

    public boolean remove(Trade tradeToRemove) {
        if (tradeToRemove.isReduction()) {
            removeNoChecks(tradeToRemove);
            return true;
        }

        List<Trade> currentTrades = getByHoldingSortedByDate(tradeToRemove.getHolding().getId());

        if (TradeUtils.isRemoveCorrect(tradeToRemove, currentTrades)) {
            removeNoChecks(tradeToRemove);
            return true;
        }

        return false;
    }

    public List<Trade> getByHoldingSortedByDate(long holdingId) {
        TypedQuery<Trade> tq = this.entityManager.createQuery(
                "SELECT t from " + Trade.class.getSimpleName() + " t " +
                        "WHERE t.holding.id = :holdingId " +
                        "ORDER BY t.date", Trade.class).setParameter("holdingId", holdingId);

        List<Trade> results = tq.getResultList();

        return (results == null) ? new ArrayList<>() : results;
    }

//    public LocalDate getEarliestTradingDateByPortfolio(long portfolioId) {
//        TypedQuery<LocalDate> tq = this.entityManager.createQuery(
//                "SELECT MIN(t.date) FROM " + Trade.class.getSimpleName() + " t " +
//                        "WHERE t.holding.portfolio.id = :portfolioId", LocalDate.class);
//        tq.setParameter("portfolioId", portfolioId);
//    }
}
