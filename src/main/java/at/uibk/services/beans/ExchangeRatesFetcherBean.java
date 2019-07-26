package at.uibk.services.beans;


import at.uibk.apis.exchangeRates.endpoints.ExchangeRatesEndPoint;
import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRates;
import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRatesTimeSeries;
import at.uibk.model.Currency;
import at.uibk.model.mainEntities.ExchangeRatesEntity;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;

@Singleton
@Startup
@Named
public class ExchangeRatesFetcherBean {
    private static final LocalDate START_DATE = LocalDate.of(2004, 1, 1);

    @Inject
    private ExchangeRatesEndPoint exchangeRatesEndPoint;

    @EJB
    private ExchangeRatesBean exchangeRatesBean;

    public ExchangeRates getCurrent(){
        return exchangeRatesBean.getLatest(Currency.EUR);
    }

    @PostConstruct
    @Schedule(dayOfWeek = "1-5", hour = "17", minute = "15")
    public void update(){
        LocalDate startDate = exchangeRatesBean.getLatestDate();
        if(startDate == null)
            startDate = START_DATE;
        else
            startDate = startDate.plusDays(1);

        long start = System.currentTimeMillis();
        ExchangeRatesTimeSeries exchangeRatesTimeSeries = exchangeRatesEndPoint.getHistoryWithBase(startDate, LocalDate.now(), Currency.EUR);

        long start02 = System.currentTimeMillis();

        exchangeRatesTimeSeries.getRates().forEach((date, currencyToRateMap) ->
            exchangeRatesBean.persist(ExchangeRatesEntity.of(date, currencyToRateMap))
        );

        long time = System.currentTimeMillis() - start;
        long time02 = System.currentTimeMillis() - start02;

        System.out.println("Gesamt: " + time/1000.0 + " , speichern: " + time02/1000.0);
    }
}
