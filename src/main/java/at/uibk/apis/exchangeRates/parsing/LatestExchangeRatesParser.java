package at.uibk.apis.exchangeRates.parsing;

import at.uibk.apis.exchangeRates.exceptions.ExchangeRateException;
import at.uibk.apis.exchangeRates.responseDataTypes.ExchangeRates;
import at.uibk.model.Currency;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class LatestExchangeRatesParser extends ExchangeRatesBaseParser<ExchangeRates> {

    protected ExchangeRates resolve(JSONObject rootObject) {
        try {
            Currency base = Currency.valueOf(rootObject.getString(JsonKeys.BASE.getValue()));

            LocalDate date = ParserUtils.parseDate(rootObject.getString(JsonKeys.DATE.getValue()));

            Map<Currency, BigDecimal> rates = getRates(rootObject.getJSONObject(JsonKeys.RATES.getValue()));

            if (base == ParserUtils.DEFAULT_BASE) { // if base is EUR, add "EUR" to map
                rates.put(base, BigDecimal.ONE);
            }

            return new ExchangeRates(base, date, rates);
        } catch (JSONException e) {
            throw new ExchangeRateException("General result format changed!");
        } catch (IllegalArgumentException e){
            throw new ExchangeRateException("Currency not supported!");
        }
    }

    private Map<Currency, BigDecimal> getRates(JSONObject ratesObject) {

        HashMap<Currency, BigDecimal> result = new HashMap<>();

        for (String currencySymbol : ratesObject.keySet()) {
            if(ParserUtils.isSupportedCurrency(currencySymbol)) {
                result.put(Currency.valueOf(currencySymbol), ratesObject.getBigDecimal(currencySymbol));
            }
        }

        return result;
    }
}
