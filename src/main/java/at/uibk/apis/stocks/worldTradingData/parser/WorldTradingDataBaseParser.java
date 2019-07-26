package at.uibk.apis.stocks.worldTradingData.parser;

import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.apis.stocks.worldTradingData.exceptions.WorldTradingDataException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

abstract class WorldTradingDataBaseParser<T> implements Serializable {
	private static final String MESSAGE_KEY = "Message";

	protected abstract T resolve(JSONObject rootObject);
	
	public T parse(String json) {
        if(json == null || json.isEmpty())
            throw new WorldTradingDataException("Empty JSON String");

		JSONObject rootObject;

		try {
			rootObject = new JSONObject(json);
		} catch (JSONException e) {
			throw new WorldTradingDataException("Syntax Error or duplicate key in JSON response!");
		}

		if(!rootObject.optString(MESSAGE_KEY).isEmpty()) {
			throw new AlphaVantageException(rootObject.getString(MESSAGE_KEY));
		}

		return resolve(rootObject);
	}
}
