package at.uibk.services.beans;

import at.uibk.model.mainEntities.Holding;
import at.uibk.model.mainEntities.StockIdentifier;

import javax.ejb.Stateless;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Stateless
public class HoldingBean {
    @PersistenceContext
    private EntityManager entityManager;

    private static final String FIND_ALL_BY_PORTFOLIO =
            "SELECT holding FROM Holding holding WHERE holding.portfolio.id = :portfolioId";

    public Holding find(long holdingId) {
        return entityManager.find(Holding.class, holdingId);
    }

    public void persist(Holding holding) {
        entityManager.persist(holding);
    }

    public void remove(Holding holding) {
        holding = find(holding.getId());
        entityManager.remove(holding);
    }

    public boolean isUnique(String fullyQualifiedSymbol, long portfolioId) {
        List<Holding> holdings = getAllByPortfolio(portfolioId);

        return holdings.stream().
                allMatch(holding -> !Objects.equals(holding.getFullyQualifiedSymbol(), fullyQualifiedSymbol));
    }

    public Holding getBy(StockIdentifier stockIdentifier, long portfolioId) {
        TypedQuery<Holding> typedQuery = entityManager.createQuery("SELECT h from " + Holding.class.getSimpleName() + " h " +
                "WHERE h.portfolio.id = :portfolioId AND " +
                "h.stockIdentifier.symbol = :symbol AND " +
                "h.stockIdentifier.stockExchange = :stockExchange", Holding.class);

        typedQuery.setParameter("portfolioId", portfolioId);
        typedQuery.setParameter("symbol", stockIdentifier.getSymbol());
        typedQuery.setParameter("stockExchange", stockIdentifier.getStockExchange());

        List<Holding> result = typedQuery.getResultList();

        if (result == null || result.isEmpty())
            return null;
        if (result.size() == 1)
            return result.get(0);

        throw new IllegalStateException("Two holdings for " + result.get(0).getFullyQualifiedSymbol() + " in " + result.get(0).getPortfolio().getName());
    }

    public List<Holding> getAllByPortfolio(long portfolioId) {
        TypedQuery<Holding> typedQuery = entityManager.createQuery(FIND_ALL_BY_PORTFOLIO, Holding.class);
        typedQuery.setParameter("portfolioId", portfolioId);

        List<Holding> results = typedQuery.getResultList();

        return results == null ? new ArrayList<>() : results;
    }

    public List<Holding> getAllEagerByPortfolio(long portfolioId) {
        TypedQuery<Holding> typedQuery = entityManager.createQuery(FIND_ALL_BY_PORTFOLIO, Holding.class);
        typedQuery.setParameter("portfolioId", portfolioId);

        EntityGraph entityGraph = entityManager.getEntityGraph("HoldingWithTradesDividends");

        typedQuery.setHint("javax.persistence.fetchgraph", entityGraph);

        List<Holding> result = typedQuery.getResultList();

        return result == null ? new ArrayList<>() : result;
    }

    public List<Holding> getAllNotClosedEagerByPortfolio(long portfolioId) {
        return getAllEagerByPortfolio(portfolioId).stream()
                .filter(holding -> !holding.isClosed())
                .collect(Collectors.toList());
    }

}
