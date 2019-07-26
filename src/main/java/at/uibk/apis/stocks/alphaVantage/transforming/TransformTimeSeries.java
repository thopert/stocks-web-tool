package at.uibk.apis.stocks.alphaVantage.transforming;

import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeries;
import at.uibk.apis.stocks.alphaVantage.responseDataTypes.StockTimeSeriesEntry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class TransformTimeSeries {

    public static StockTimeSeries fromDailyToMonthlyTimeSeries(StockTimeSeries dayTimeSeries) {

        TreeMap<LocalDate, StockTimeSeriesEntry> dailyEntries = dayTimeSeries.getEntries();

        List<LocalDate> sortedDates = new ArrayList<>(dailyEntries.keySet());

        List<LocalDate[]> datePairs = getDatePairs(sortedDates);

        TreeMap<LocalDate, StockTimeSeriesEntry> monthEntries = getMonthEntries(datePairs, dailyEntries);

        LocalDate lastRefreshed = monthEntries.lastKey();

        String fullQualifiedSymbol = dayTimeSeries.getFullyQualifiedSymbol();

        return new StockTimeSeries(fullQualifiedSymbol, lastRefreshed, monthEntries);
    }

    private static List<LocalDate[]> getDatePairs(List<LocalDate> sortedDates) {

        List<LocalDate[]> datePairs = new ArrayList<>();

        int i = 0;
        while (i < sortedDates.size()) {
            LocalDate first = sortedDates.get(i);

            int j = i + 1;

            while (j < sortedDates.size()) {
                if (first.getMonth() != sortedDates.get(j).getMonth()) {
                    break;
                }
                j++;
            }

            LocalDate second = sortedDates.get(j - 1);

            datePairs.add(new LocalDate[]{first, second});

            i = j;
        }

        return datePairs;
    }

    private static TreeMap<LocalDate, StockTimeSeriesEntry> getMonthEntries(List<LocalDate[]> datePairs,
                                                                           TreeMap<LocalDate, StockTimeSeriesEntry> dayEntries) {

        TreeMap<LocalDate, StockTimeSeriesEntry> resultMap = new TreeMap<>();

        for (LocalDate[] datePair : datePairs) {
            NavigableMap<LocalDate, StockTimeSeriesEntry> monthlyEntries = dayEntries.subMap(datePair[0], true,
                    datePair[1], true);

            StockTimeSeriesEntry monthlyEntry = getMonthEntry(monthlyEntries);

            resultMap.put(monthlyEntry.getDate(), monthlyEntry);
        }

        return resultMap;
    }

    private static StockTimeSeriesEntry getMonthEntry(NavigableMap<LocalDate, StockTimeSeriesEntry> monthEntries) {
        StockTimeSeriesEntry firstOfMonth = monthEntries.firstEntry().getValue();
        StockTimeSeriesEntry lastOfMonth = monthEntries.lastEntry().getValue();

        BigDecimal open = firstOfMonth.getClose();
        BigDecimal close = lastOfMonth.getClose();
        BigDecimal high = open;
        BigDecimal low = open;
        long volume = 0;

        for (StockTimeSeriesEntry value : monthEntries.values()) {
            if (high.compareTo(value.getHigh()) < 0) {
                high = value.getHigh();
            }

            if (low.compareTo(value.getLow()) > 0) {
                low = value.getLow();
            }
            volume += value.getVolume();
        }

        return new StockTimeSeriesEntry(lastOfMonth.getDate(), open, high, low, close, volume);
    }

//    public static List<LocalDate[]> getDatePairs(StockTimeSeries daysTimeSeries) {
//
//        TreeMap<LocalDate, StockTimeSeriesEntry> dayEntries = daysTimeSeries.getEntries();
//
//        List<LocalDate> sortedDates = new ArrayList<>(dayEntries.keySet());
//
//        return getDatePairs(sortedDates);
//    }

}
