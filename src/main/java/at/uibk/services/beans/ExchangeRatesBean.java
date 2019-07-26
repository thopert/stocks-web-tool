package at.uibk.services.beans;

import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRates;
import at.uibk.model.Currency;
import at.uibk.model.mainEntities.ExchangeRatesEntity;

import javax.ejb.Stateless;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Stateless
public class ExchangeRatesBean {

    @PersistenceContext
    private EntityManager entityManager;

    public void persist(ExchangeRatesEntity exchangeRatesEntity) {
        entityManager.persist(exchangeRatesEntity);
    }

    public ExchangeRates getLatest(Currency baseCurrency) {
        TypedQuery<ExchangeRatesEntity> tq =
                entityManager.createQuery(
                        "SELECT er " +
                                "FROM " + ExchangeRatesEntity.class.getSimpleName() + " er " +
                                "WHERE er.date = " +
                                "(SELECT MAX(e.date) FROM " + ExchangeRatesEntity.class.getSimpleName() + " e )",
                        ExchangeRatesEntity.class);
        try {
            ExchangeRatesEntity result = tq.getSingleResult();
            return result.toExchangeRates(baseCurrency);
        }catch (NoResultException e) {
            return null;
        }catch (NonUniqueResultException e){
            throw new IllegalStateException("More than one exchange rate entity per date!");
        }
    }

    public ExchangeRates getFloorEntry(LocalDate date, Currency baseCurrency) {
        TypedQuery<ExchangeRatesEntity> tq = entityManager.createQuery(
                "SELECT er " +
                        "FROM " + ExchangeRatesEntity.class.getSimpleName() + " er " +
                        "WHERE er.date = " +
                        "(SELECT MAX(er2.date) FROM " + ExchangeRatesEntity.class.getSimpleName() + " er2 " +
                        "WHERE er2.date <= :date )", ExchangeRatesEntity.class);

        tq.setParameter("date", date);

        try {
            ExchangeRatesEntity result = tq.getSingleResult();
            return result.toExchangeRates(baseCurrency);
        } catch (NoResultException e) {
            return null;
        }catch (NonUniqueResultException e){
            throw new IllegalStateException("More than one exchange rate entity per date!");
        }
    }

    public LocalDate getLatestDate() {
        TypedQuery<LocalDate> tq = entityManager.createQuery(
                        "SELECT MAX(er.date) " +
                                "FROM " + ExchangeRatesEntity.class.getSimpleName() + " er ", LocalDate.class);
        try {
            return tq.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (NonUniqueResultException e){
            throw new IllegalStateException("MAX returns more than one result!");
        }
    }

    public List<ExchangeRates> getTailList(LocalDate date, Currency baseCurrency){
        TypedQuery<ExchangeRatesEntity> tq = entityManager.createQuery(
                "SELECT er " +
                        "FROM " + ExchangeRatesEntity.class.getSimpleName() + " er " +
                        "WHERE er.date >= :date", ExchangeRatesEntity.class);
        tq.setParameter("date", date);

        List<ExchangeRatesEntity> exchangeRatesEntityList = tq.getResultList();

        return exchangeRatesEntityList.stream()
                .map(exchangeRatesEntity -> exchangeRatesEntity.toExchangeRates(baseCurrency))
                .collect(Collectors.toList());
    }

    public NavigableMap<LocalDate, ExchangeRates> getTailMap(LocalDate date, Currency baseCurrency){
        TypedQuery<ExchangeRatesEntity> tq = entityManager.createQuery(
                "SELECT er FROM " + ExchangeRatesEntity.class.getSimpleName() + " er " +
                        "WHERE er.date >= :date", ExchangeRatesEntity.class);
        tq.setParameter("date", date);

        List<ExchangeRatesEntity> exchangeRatesEntityList = tq.getResultList();

        TreeMap<LocalDate, ExchangeRates> tailMap = new TreeMap<>();
        for (ExchangeRatesEntity exchangeRatesEntity : exchangeRatesEntityList) {
            ExchangeRates exchangeRates = exchangeRatesEntity.toExchangeRates(baseCurrency);
            tailMap.put(exchangeRates.getDate(), exchangeRates);
        }
        return tailMap;
    }

    public NavigableMap<LocalDate, ExchangeRates> getFloorTailMap(LocalDate date, Currency baseCurrency){
        TypedQuery<ExchangeRatesEntity> tq = entityManager.createQuery(
                "SELECT ere FROM " + ExchangeRatesEntity.class.getSimpleName() + " ere " +
                        "WHERE ere.date >= " +
                        "(SELECT MAX(ere2.date) FROM " + ExchangeRatesEntity.class.getSimpleName() + " ere2 " +
                        "WHERE ere2.date <= :date)",  ExchangeRatesEntity.class);
        tq.setParameter("date", date);

        List<ExchangeRatesEntity> exchangeRatesEntityList = tq.getResultList();

        TreeMap<LocalDate, ExchangeRates> floorTailMap = new TreeMap<>();
        for (ExchangeRatesEntity exchangeRatesEntity : exchangeRatesEntityList) {
            ExchangeRates exchangeRates = exchangeRatesEntity.toExchangeRates(baseCurrency);
            floorTailMap.put(exchangeRates.getDate(), exchangeRates);
        }
        return floorTailMap;
    }
}
