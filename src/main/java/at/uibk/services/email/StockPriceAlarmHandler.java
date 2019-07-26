package at.uibk.services.email;

import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockQuote;
import at.uibk.apis.stocks.alphaVantage.tools.AlphaVantageEndPointsTool;
import at.uibk.model.mainEntities.StockPriceAlarm;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Startup
public class StockPriceAlarmHandler {
    @EJB
    private StockPriceAlarmBean stockPriceAlarmBean;

    @Inject
    private AlphaVantageEndPointsTool alphaVantageEndPointsTool;

    @Inject
    private EmailNotifier emailNotifier;

    private Map<String, StockQuote> symbolToQuoteMap;

//    @PostConstruct
    @Schedule(dayOfWeek = "1-5", hour = "18", minute = "15")
    public void handleStockPriceAlarms() {

        List<StockPriceAlarm> activeAlarms = stockPriceAlarmBean.getAllActive();

        if(activeAlarms.isEmpty()){
            return;
        }

        Set<String> symbols = activeAlarms.stream()
                .map(stockPriceAlarm -> stockPriceAlarm.getHolding().getFullyQualifiedSymbol())
                .collect(Collectors.toSet());

        symbolToQuoteMap = alphaVantageEndPointsTool.getQuote(symbols);

        notifyUsersOfTriggeredAlarms(activeAlarms);
    }

    private void notifyUsersOfTriggeredAlarms(List<StockPriceAlarm> activeAlarms) {
        Map<StockPriceAlarm, BigDecimal> triggeredAlarms = getTriggeredAlarms(activeAlarms);

        for (Map.Entry<StockPriceAlarm, BigDecimal> triggeredAlarm : triggeredAlarms.entrySet()) {
            emailNotifier.sendAlarm(triggeredAlarm.getKey(), triggeredAlarm.getValue());
        }
    }

    private Map<StockPriceAlarm, BigDecimal> getTriggeredAlarms(List<StockPriceAlarm> stockPriceAlarms) {
        Map<StockPriceAlarm, BigDecimal> triggeredAlarms = new HashMap<>();

        for (StockPriceAlarm stockPriceAlarm : stockPriceAlarms) {
            String symbol = stockPriceAlarm.getHolding().getFullyQualifiedSymbol();
            StockQuote stockQuote = symbolToQuoteMap.get(symbol);
            BigDecimal dayHigh = stockQuote.getHigh();
            BigDecimal dayLow = stockQuote.getLow();

            if (stockPriceAlarm.isTriggeredBy(dayHigh)) {
                triggeredAlarms.put(stockPriceAlarm, dayHigh);
            } else if(stockPriceAlarm.isTriggeredBy(dayLow)){
                triggeredAlarms.put(stockPriceAlarm, dayLow);
            }
        }
        return triggeredAlarms;
    }
}
