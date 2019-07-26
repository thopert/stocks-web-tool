package at.uibk.apis.exchangeRates.parsing;

import at.uibk.apis.exchangeRates.exceptions.ExchangeRateException;
import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRatesTimeSeries;
import at.uibk.model.Currency;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ExchangeRatesTimeSeriesParser extends ExchangeRatesBaseParser<ExchangeRatesTimeSeries> {

    protected ExchangeRatesTimeSeries resolve(JSONObject rootObject) {
        try {
            Currency base = Currency.valueOf(rootObject.getString(JsonKeys.BASE.getValue()));

            LocalDate start = ParserUtils.parseDate(rootObject.getString(JsonKeys.START.getValue()));

            LocalDate end = ParserUtils.parseDate(rootObject.getString(JsonKeys.END.getValue()));

            Map<LocalDate, Map<Currency, BigDecimal>> rates = getRates(rootObject.getJSONObject(JsonKeys.RATES.getValue()), base);

            return  new ExchangeRatesTimeSeries(start, end, base, rates);
        } catch (JSONException e) {
            throw new ExchangeRateException("General result format changed!");
        }catch (IllegalArgumentException e){
            throw new ExchangeRateException("Unknown currency!");
        }
    }

    private Map<LocalDate, Map<Currency, BigDecimal>> getRates(JSONObject ratesObject, Currency base) {

        Map<LocalDate, Map<Currency, BigDecimal>> result = new HashMap<>();

        // if base is EUR, then EUR is not added by default and must be added manually
        boolean addDefaultFlag = base == ParserUtils.DEFAULT_BASE;

        for (String date : ratesObject.keySet()) {
            LocalDate localDate = LocalDate.parse(date, ParserUtils.DATE_FORMATTER);
            Map<Currency, BigDecimal> ratesPerDate = parseRatesEntry(ratesObject.getJSONObject(date));
            if(addDefaultFlag){
                ratesPerDate.put(ParserUtils.DEFAULT_BASE, BigDecimal.ONE);
            }
            result.put(localDate, ratesPerDate);
        }
        return result;
    }

    private Map<Currency, BigDecimal> parseRatesEntry(JSONObject ratesEntry) {

        Map<Currency, BigDecimal> rateMap = new HashMap<>();

        for (String currencySymbol : ratesEntry.keySet()) {
            if(ParserUtils.isSupportedCurrency(currencySymbol)) {
                rateMap.put(Currency.valueOf(currencySymbol), ratesEntry.getBigDecimal(currencySymbol));
            }
        }
        return rateMap;
    }
}
