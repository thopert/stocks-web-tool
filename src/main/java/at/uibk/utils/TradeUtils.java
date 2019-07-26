package at.uibk.utils;

import at.uibk.model.Currency;
import at.uibk.model.mainEntities.Trade;
import at.uibk.model.mainEntities.TradeType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TradeUtils {

    public static boolean isPersistCorrect(Trade tradeToPersist, List<Trade> referenceTrades) {
        // nothing to do if trade is not a reduction
        if (!tradeToPersist.isReduction()) {
            return true;
        }

        List<Trade> referenceTradesCopy = new ArrayList<>(referenceTrades);

        // total list
        referenceTradesCopy.add(tradeToPersist);

        Predicate<Trade> isReduction = Trade::isReduction;
        Predicate<Trade> isAfterwards = t -> t.getDate().compareTo(tradeToPersist.getDate()) >= 0;

        // retrieve reductions to check
        List<Trade> reductionsToCheck =
                referenceTradesCopy.stream().filter(isReduction.and(isAfterwards)).collect(Collectors.toList());

        // go through all reduction steps after equals the one to add
        for (Trade trade : reductionsToCheck) {
            referenceTradesCopy.remove(trade);
            if (!isReductionStepCorrect(trade, referenceTradesCopy)) {
                return false;
            }
            referenceTradesCopy.add(trade);
        }

        return true;
    }

    private static boolean isReductionStepCorrect(Trade reductionTrade, List<Trade> referenceTrades) {
        List<Trade> tradesUntilReduction = referenceTrades.stream()
                .filter(trade -> trade.getDate().compareTo(reductionTrade.getDate()) <= 0)
                .collect(Collectors.toList());

        // check if number of shares till reduction trade is >= than reduction size
        return getNumberOfShares(tradesUntilReduction).compareTo(reductionTrade.getQuantity()) >= 0;
    }


    public static boolean isRemoveCorrect(Trade tradeToRemove, List<Trade> referenceTrades) {
        // nothing to do if trade is a reduction (shares are gained)
        if (tradeToRemove.isReduction()) {
            return true;
        }

        List<Trade> referenceTradesCopy = new ArrayList<>(referenceTrades);

        // checking weather the removal is correct, is the same as checking weather adding
        // a new reduction with equal specs as the removal is checked for persisting
        Trade dummyReductionTrade = new Trade(tradeToRemove);
        dummyReductionTrade.setType(TradeType.SELL);
        dummyReductionTrade.setId(-1L); // choose unused id
        return isPersistCorrect(dummyReductionTrade, referenceTradesCopy);
    }

    public static boolean isMergeCorrect(Trade tradeToMerge, List<Trade> referenceTrades) {
        // nothing to do if trade is not a reduction
        if (!tradeToMerge.isReduction()) {
            return true;
        }

        List<Trade> referenceTradesCopy = new ArrayList<>(referenceTrades);

        // delete trade that is merged from list and check if toMerge can be persisted
        referenceTradesCopy.removeIf(trade -> trade.getId() == tradeToMerge.getId());

        return isPersistCorrect(tradeToMerge, referenceTradesCopy);
    }

    public static BigDecimal getNumberOfShares(List<Trade> trades) {
        BigDecimal result = BigDecimal.ZERO;
        for (Trade trade : trades) {
            if (trade.isReduction()) {
                result = result.subtract(trade.getQuantity());
            } else {
                result = result.add(trade.getQuantity());
            }
        }
        return result;
    }

    public static boolean isClosedPosition(List<Trade> trades) {
        return getNumberOfShares(trades).compareTo(BigDecimal.ZERO) == 0;
    }

    public static List<Currency> getSelectableCurrencies(Currency portfolioCurrency, Currency holdingCurrency){
        if(holdingCurrency == portfolioCurrency)
            return Arrays.asList(holdingCurrency);

        return Arrays.asList(holdingCurrency,portfolioCurrency);
    }

    public static List<Trade> getTradesUntil(List<Trade> trades, LocalDate deadLine){
        return trades.stream()
                .filter(trade -> trade.getDate().compareTo(deadLine) <= 0)
                .collect(Collectors.toList());
    }

    public static Optional<LocalDate> getEarliestTradeDate(List<Trade> trades){
        return trades.stream()
                .map(Trade::getDate)
                .min(Comparator.naturalOrder());
    }

}
