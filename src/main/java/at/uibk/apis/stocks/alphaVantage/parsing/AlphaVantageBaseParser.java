package at.uibk.apis.stocks.alphaVantage.parsing;

import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.apis.stocks.alphaVantage.exceptions.CallFrequencyExceededException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

abstract class AlphaVantageBaseParser<T> implements Serializable {
	private static final String ERROR_KEY = "Error Message";
	private static final String NOTE_KEY = "Note";

	protected abstract T resolve(JSONObject rootObject);
	
	public T parse(String json) {
        if(json == null || json.isEmpty())
            throw new AlphaVantageException("Empty JSON String");

		JSONObject rootObject;

		try {
			rootObject = new JSONObject(json);
		} catch (JSONException e) {
			throw new AlphaVantageException("Syntax Error in JSON response!");
		}

		if(!rootObject.optString(ERROR_KEY).isEmpty()) {
			throw new AlphaVantageException(rootObject.getString(ERROR_KEY));
		}
		if(!rootObject.optString(NOTE_KEY).isEmpty()) {
			throw new CallFrequencyExceededException(rootObject.getString(NOTE_KEY));
		}

		return resolve(rootObject);
	}
}
