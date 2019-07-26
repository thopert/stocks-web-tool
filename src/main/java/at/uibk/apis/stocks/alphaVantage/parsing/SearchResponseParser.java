package at.uibk.apis.stocks.alphaVantage.parsing;

import at.uibk.apis.stocks.alphaVantage.exceptions.AlphaVantageException;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockSearchResult;
import at.uibk.model.Currency;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchResponseParser extends AlphaVantageBaseParser<List<StockSearchResult>> {
    private static final String KEY = "bestMatches";

    private StockSearchResult parseMatch(JSONObject match) {
        StockSearchResult stockSearchResult = new StockSearchResult();
        try {
            stockSearchResult.setFullyQualifiedSymbol(match.getString("1. symbol"));
            stockSearchResult.setName(match.getString("2. name"));
            stockSearchResult.setType(match.getString("3. type"));
            stockSearchResult.setRegion(match.getString("4. region"));
            stockSearchResult.setMarketOpen(ParserUtils.parseTime(match.getString("5. marketOpen")));
            stockSearchResult.setMarketClose(ParserUtils.parseTime(match.getString("6. marketClose")));
            stockSearchResult.setTimeZone(match.getString("7. timezone"));
            if(ParserUtils.isSupportedCurrency(match.getString("8. currency"))) {
                stockSearchResult.setCurrency(Currency.valueOf(match.getString("8. currency")));
            }
            stockSearchResult.setMatchScore(match.getDouble("9. matchScore"));

            return stockSearchResult;
        } catch (JSONException e) {
            throw new AlphaVantageException("General Result format has changed!");
        } catch (IllegalArgumentException e) {
            throw new AlphaVantageException("Unknown currency!");
        } catch (Exception e) {
            throw new AlphaVantageException("Unknown failure!");
        }
    }

    @Override
    protected List<StockSearchResult> resolve(JSONObject rootObject) {
        JSONArray bestMatches;
        try {
            bestMatches = rootObject.getJSONArray(KEY);
        }catch (JSONException e) {
            throw new AlphaVantageException("General Result format has changed!");
        }

        List<StockSearchResult> searchResults = new ArrayList<>();

        for (int i = 0; i < bestMatches.length(); i++) {
            JSONObject match = bestMatches.getJSONObject(i);
            searchResults.add(parseMatch(match));
        }
        return searchResults;
    }
}
