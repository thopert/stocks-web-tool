package at.uibk.services.email;

import at.uibk.model.mainEntities.StockPriceAlarm;
import at.uibk.model.mainEntities.User;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.*;

@Stateless
public class StockPriceAlarmBean {

    @PersistenceContext
    private EntityManager entityManager;

    public void persist(StockPriceAlarm stockPriceAlarm){
        entityManager.persist(stockPriceAlarm);
    }

    public void merge(StockPriceAlarm stockPriceAlarm){
        this.entityManager.merge(stockPriceAlarm);
    }

    public void remove(StockPriceAlarm stockPriceAlarm){
        stockPriceAlarm = entityManager.find(StockPriceAlarm.class, stockPriceAlarm.getId());
        if (stockPriceAlarm != null) {
            entityManager.remove(stockPriceAlarm);
        }
    }

    public List<StockPriceAlarm> getAllActive(){
        TypedQuery<StockPriceAlarm> typedQuery = entityManager.createQuery(
                "SELECT stockPriceAlarm " +
                        "FROM " + StockPriceAlarm.class.getSimpleName() + " stockPriceAlarm " +
                        "WHERE stockPriceAlarm.activated = " + true, StockPriceAlarm.class);

        List<StockPriceAlarm> result = typedQuery.getResultList();

        return result == null ? new ArrayList<>() : result;
    }

    public Map<User, List<StockPriceAlarm>> getAllActiveGroupedByUser(){
        Query query = entityManager.createQuery(
                "SELECT stockPriceAlarm.holding.portfolio.user, stockPriceAlarm " +
                        "FROM " + StockPriceAlarm.class.getSimpleName() + " stockPriceAlarm ");

        List<Object[]> results = query.getResultList();

        if(results == null){
            return new HashMap<>();
        }

        return groupAlarmsByUser(results);
    }

    private HashMap<User, List<StockPriceAlarm>> groupAlarmsByUser(List<Object[]> results) {
        HashMap<User, List<StockPriceAlarm>> userToAlarmsMap = new HashMap<>();
        for(Object[] row: results){
            User user = (User) row[0];
            StockPriceAlarm stockPriceAlarm = (StockPriceAlarm) row[1];

            List<StockPriceAlarm> alarms = userToAlarmsMap.get(user);
            if(alarms != null){
                alarms.add(stockPriceAlarm);
            } else{
                alarms = new ArrayList<>();
                alarms.add(stockPriceAlarm);
            }
            userToAlarmsMap.put(user, alarms);
        }
        return userToAlarmsMap;
    }

    public TreeMap<String, List<StockPriceAlarm>> getAllActiveGroupedBySymbol(){
        List<StockPriceAlarm> allActiveAlarms = getAllActive();

        return groupBySymbol(allActiveAlarms);
    }

    private TreeMap<String, List<StockPriceAlarm>> groupBySymbol(List<StockPriceAlarm> allActiveAlarms) {
        TreeMap<String, List<StockPriceAlarm>> symbolToAlarmsMap = new TreeMap<>();

        for(StockPriceAlarm stockPriceAlarm: allActiveAlarms){
            String symbol = stockPriceAlarm.getHolding().getFullyQualifiedSymbol();

            List<StockPriceAlarm> alarmsContained = symbolToAlarmsMap.get(symbol);

            if(alarmsContained != null){
                alarmsContained.add(stockPriceAlarm);
            }else{
                alarmsContained = new ArrayList<>();
                alarmsContained.add(stockPriceAlarm);
            }

            symbolToAlarmsMap.put(symbol, alarmsContained);
        }
        return symbolToAlarmsMap;
    }

    public List<StockPriceAlarm> getAllByPortfolio(long portfolioId){
        TypedQuery<StockPriceAlarm> typedQuery = entityManager.createQuery(
                "SELECT stockPriceAlarm " +
                        "FROM " + StockPriceAlarm.class.getSimpleName() + " stockPriceAlarm " +
                        "WHERE stockPriceAlarm.holding.portfolio.id = :portfolioId", StockPriceAlarm.class);
        typedQuery.setParameter("portfolioId", portfolioId);

        List<StockPriceAlarm> results = typedQuery.getResultList();

        return results == null ? new ArrayList<>() : results;
    }

}
