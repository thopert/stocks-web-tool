package at.uibk.services.beans;

import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockExchange;
import at.uibk.model.mainEntities.Instrument;
import at.uibk.model.mainEntities.StockIdentifier;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class InstrumentBean {

    @PersistenceContext
    private EntityManager entityManager;

    public Instrument find(long instrumentId) {
        return entityManager.find(Instrument.class, instrumentId);
    }

    private List<Instrument> findByColumn(String column, String value) {
        TypedQuery<Instrument> typedQuery = entityManager.createQuery(
                "SELECT i " +
                        "FROM " + Instrument.class.getSimpleName() + " i " +
                        "WHERE i." + column + " = :value", Instrument.class)
                .setParameter("value", value);

        List<Instrument> results = typedQuery.getResultList();

        return results == null ? new ArrayList<>() : results;
    }

    public Instrument findByStockIdentifier(StockIdentifier stockIdentifier) {
        TypedQuery<Instrument> typedQuery = entityManager.createQuery(
                "SELECT i FROM " + Instrument.class.getSimpleName() + " i " +
                        "WHERE i.stockIdentifier.symbol = :symbol " +
                        "AND i.stockIdentifier.stockExchange = :stockExchange", Instrument.class);
        typedQuery.setParameter("symbol", stockIdentifier.getSymbol());
        typedQuery.setParameter("stockExchange", stockIdentifier.getStockExchange());

        List<Instrument> results = typedQuery.getResultList();

        if (results == null || results.isEmpty())
            return null;

        if (results.size() == 1)
            return results.get(0);

        throw new IllegalStateException("Instrument " + results.get(0).getFullyQualifiedSymbol() + " exists several " +
                "times!");
    }

    public List<Instrument> findBySymbol(String symbol) {
        String normalizedSymbol = normalizeSymbol(symbol);
        return findByColumn("stockIdentifier.symbol", normalizedSymbol);
    }

    public List<Instrument> findByIsin(String isin) {
        return findByColumn("isin", isin);
    }

    public List<Instrument> findByWkn(String wkn) {
        return findByColumn("wkn", wkn);
    }

    public List<Instrument> findByName(String name) {
        TypedQuery<Instrument> typedQuery = entityManager.createQuery(
                "SELECT i FROM " + Instrument.class.getSimpleName() + " i " +
                        "WHERE i.name LIKE '%" + name + "%'", Instrument.class);
        System.out.println(typedQuery.toString());

        List<Instrument> results = typedQuery.getResultList();

        return results == null ? new ArrayList<>() : results;
    }

    public List<Instrument> searchAll(String keyWords, StockExchange stockExchange) {
        TypedQuery<Instrument> typedQuery = entityManager.createQuery(
                "SELECT i FROM " + Instrument.class.getSimpleName() + " i " +
                        "WHERE i.stockIdentifier.stockExchange = :stockExchange " +
                        "AND (i.stockIdentifier.symbol LIKE '%" + keyWords + "%' " +
                        "OR i.name LIKE '%" + keyWords + "%' " +
                        "OR i.isin LIKE '%" + keyWords + "%') " +
                        "ORDER BY i.stockIdentifier.symbol", Instrument.class);

        typedQuery.setParameter("stockExchange", stockExchange);

        List<Instrument> results = typedQuery.getResultList();

        List<Instrument> symbolMatches = results.stream()
                .filter(instrument -> instrument.getSymbol().equalsIgnoreCase(keyWords))
                .collect(Collectors.toList());

        return symbolMatches.isEmpty() ? results : symbolMatches;
    }

    /* search logic */

    public List<Instrument> searchAll(String searchTerm, int... maxResults) {
        TypedQuery<Instrument> typedQuery = entityManager.createQuery(
                "SELECT i FROM " + Instrument.class.getSimpleName() + " i " +
                        "WHERE i.stockIdentifier.symbol LIKE '%" + searchTerm + "%' " +
                        "OR i.name LIKE '%" + searchTerm + "%' " +
                        "OR i.isin LIKE '%" + searchTerm + "%' " +
                        "ORDER BY i.stockIdentifier.symbol", Instrument.class);

        handleMaxResultParameter(typedQuery, maxResults);

        List<Instrument> results = typedQuery.getResultList();

        List<Instrument> symbolMatches = results.stream()
                .filter(instrument -> instrument.getSymbol().equalsIgnoreCase(searchTerm))
                .collect(Collectors.toList());

        return symbolMatches.isEmpty() ? results : symbolMatches;
    }

    public List<Instrument> searchBySymbol(String searchTerm, int... maxResults) {
        String normalizedSymbol = normalizeSymbol(searchTerm);
        return searchByColumn("stockIdentifier.symbol", normalizedSymbol, maxResults);
    }

    public List<Instrument> searchByName(String searchTerm, int... maxResults) {
        return searchByColumn("name", searchTerm, maxResults);
    }

    public List<Instrument> searchByIsin(String searchTerm, int... maxResults) {
        return searchByColumn("isin", searchTerm, maxResults);
    }

    public List<Instrument> searchByWkn(String searchTerm, int... maxResults) {
        return searchByColumn("wkn", searchTerm, maxResults);
    }

    public List<Instrument> searchByNameIsinWkn(String searchTerm, int... maxResults) {
        TypedQuery<Instrument> typedQuery = entityManager.createQuery(
                "SELECT i FROM " + Instrument.class.getSimpleName() + " i " +
                        "WHERE i.name LIKE '%" + searchTerm + "%' " +
                        "OR i.isin LIKE '%" + searchTerm + "%' " +
                        "OR i.wkn LIKE '%" + searchTerm + "%' ", Instrument.class);

        handleMaxResultParameter(typedQuery, maxResults);

        List<Instrument> results = typedQuery.getResultList();

        return results == null ? new ArrayList<>() : results;
    }


    private List<Instrument> searchByColumn(String columnName, String searchTerm, int... maxResults) {
        TypedQuery<Instrument> typedQuery = entityManager.createQuery(
                "SELECT i FROM " + Instrument.class.getSimpleName() + " i " +
                        "WHERE i." + columnName + " LIKE '%" + searchTerm + "%'", Instrument.class);

        handleMaxResultParameter(typedQuery, maxResults);

        List<Instrument> results = typedQuery.getResultList();

        return results == null ? new ArrayList<>() : results;
    }

    /* helpers */

    private void handleMaxResultParameter(TypedQuery<Instrument> typedQuery, int[] maxResults) {
        if (maxResults != null && maxResults.length > 0) {
            if (maxResults[0] > 0)
                typedQuery.setMaxResults(maxResults[0]);
        }
    }

    private String normalizeSymbol(String symbol) {
        return symbol.toUpperCase();
    }

}
