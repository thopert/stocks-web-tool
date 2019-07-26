package at.uibk.apis.exchangeRates.parsing;

import at.uibk.apis.exchangeRates.exceptions.ExchangeRateException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public abstract class ExchangeRatesBaseParser<T> implements Serializable {

    public T parse(String json){
        if (json == null || json.isEmpty())
            throw new ExchangeRateException("JSON string is empty or null!");

        JSONObject rootObject = null;

        try {
            rootObject = new JSONObject(json);
        } catch (JSONException e) {
            throw new ExchangeRateException("Syntax error in JSON string!");
        }

        String error = rootObject.optString(JsonKeys.ERROR.getValue());

        if(!error.isEmpty())
            throw new ExchangeRateException(error);

        return resolve(rootObject);
    }

    abstract protected T resolve(JSONObject rootObject);
}
