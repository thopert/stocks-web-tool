package at.uibk.apis.stocks.alphaVantage.parsing;

import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import org.json.JSONException;
import org.json.JSONObject;

abstract class StockTimeSeriesBaseParser<T> extends AlphaVantageBaseParser<T> {
	private final static String META_DATA_KEY = "Meta Data";
	
	abstract protected String getTimeSeriesType();
	
	abstract protected T resolve(JSONObject metaData, JSONObject
	timeSeries);
	
	@Override
	protected T resolve(JSONObject rootObject) {

		JSONObject metaData;
		JSONObject timeSeries;
		try {
			metaData = rootObject.getJSONObject(META_DATA_KEY);
			timeSeries = rootObject.getJSONObject(this.getTimeSeriesType());
		} catch (JSONException e) {
			throw new AlphaVantageException("Meta data format changed!");
		}
		return resolve(metaData, timeSeries);
	}

}
