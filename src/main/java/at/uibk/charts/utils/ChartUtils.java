package at.uibk.charts.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChartUtils {
    public static final String[] COLORS = new String[]{
            "0018c6", // blue
            "c67800", // brown
            "b6009d", // pink
            "a2a2a2", // silver
            "008db6", // blue-turquoise
            "71b600", // olive
            "5000b6", // purple
            "00b0b6", // turquoise
    };

    private enum SizeType {
        NORMAL, LARGE, HUGE;
    }

    public static final String DEFAULT_COLOR = "#2c46eb";
    public static final String COLOR_GREEN = "#66bb89";
    public static final String COLOR_RED = "#c52b40";
    public static final String COLOR_MAGENTA = "#ad23a4";

    public static final DateTimeFormatter DATE_TIME_DAY = DateTimeFormatter.ofPattern("dd MMM yy");
    public static final DateTimeFormatter DATE_TIME_MONTH = DateTimeFormatter.ofPattern("MMM yy");
    public static final DateTimeFormatter DATE_TIME_YEAR = DateTimeFormatter.ofPattern("yyyy");

    public static final int IS_LARGE_LIMIT = 100; // ~ 4.5 months
    public static final int IS_HUGE_LIMIT = 1303; // ~ 7 years = 1825, ~5 years = 1303

    private static final int MAX_NUMBER_OF_LABELS = 15;
    public static final int DECIMAL_SCALE = 2;
    public static final int ROUND_HALF_EVEN = BigDecimal.ROUND_HALF_EVEN;


    private static DateTimeFormatter getDateTimeFormatter(SizeType sizeType){
        if(sizeType == SizeType.HUGE)
            return DATE_TIME_YEAR;

        if(sizeType == SizeType.LARGE)
            return DATE_TIME_MONTH;

        return DATE_TIME_DAY;
    }

    private static SizeType getSizeType(int size){
        if(size > IS_HUGE_LIMIT)
            return SizeType.HUGE;

        if(size > IS_LARGE_LIMIT)
            return SizeType.LARGE;

        return SizeType.NORMAL;
    }

    public static List<String> getLabels(Collection<LocalDate> sortedDates) {
//        boolean isLarge = sortedDates.size() > IS_LARGE_LIMIT;

        SizeType sizeType = getSizeType(sortedDates.size());

//        DateTimeFormatter dateTimeFormatter = isLarge ? DATE_TIME_MONTH : DATE_TIME_DAY;
        DateTimeFormatter dateTimeFormatter = getDateTimeFormatter(sizeType);

//        List<LocalDate> datesToAdd = getDatesToAdd(sortedDates, isLarge);
        List<LocalDate> datesToAdd = getDatesToAdd(sortedDates, sizeType);

        Iterator<LocalDate> iterator = sortedDates.iterator();

        List<String> labels = new ArrayList<>();

        while (iterator.hasNext()) {
            LocalDate date = iterator.next();

            if (datesToAdd.contains(date)) {
                labels.add(date.format(dateTimeFormatter));
                continue;
            }

            boolean isLast = !iterator.hasNext();

            labels.add(isLast ? "" : null); // make sure the grid line is drawn on last element
        }

        return labels;
    }

    private static List<LocalDate> getDatesToAdd(Collection<LocalDate> sortedDates, SizeType sizeType) {
        if(sizeType == SizeType.HUGE)
            return reduceDatesStandard(getYearlyDates(sortedDates));

        if (sizeType == SizeType.LARGE)
            return reduceDatesStandard(getMonthlyDates(sortedDates));

        return reduceDatesWithLast(sortedDates);
    }

    public static List<LocalDate> reduceDatesWithLast(Collection<LocalDate> sortedDates) {
        return reduceDates(sortedDates, true);
    }

    public static List<LocalDate> reduceDatesStandard(Collection<LocalDate> sortedDates) {
        return reduceDates(sortedDates, false);
    }

    private static List<LocalDate> reduceDates(Collection<LocalDate> sortedDates, boolean withLast) {
        int numberOfDates = sortedDates.size();

        LocalDate[] sortedDatesList = new LocalDate[numberOfDates];

        sortedDates.toArray(sortedDatesList);

        List<LocalDate> selectedDates = new ArrayList<>();

        int stepSize = (int) Math.ceil((1.0 * numberOfDates) / MAX_NUMBER_OF_LABELS);

        for (int i = 0; i < numberOfDates; i += stepSize) {
            int currentIndex = i;

            if (withLast && (i + stepSize >= numberOfDates)) {
                currentIndex = numberOfDates - 1;
            }

            selectedDates.add(sortedDatesList[currentIndex]);
        }

        return selectedDates;
    }


    public static List<LocalDate> getMonthlyDates(Collection<LocalDate> sortedDates) {
        LocalDate[] sortedDatesList = new LocalDate[sortedDates.size()];

        sortedDates.toArray(sortedDatesList);

        List<LocalDate> monthlyDates = new ArrayList<>();

        int i = 0;
        while (i < sortedDatesList.length) {

            int j = i + 1; // iterating through month
            while (j < sortedDatesList.length) { // stops when month changes or no days are left
                if (sortedDatesList[i].getMonth() != sortedDatesList[j].getMonth()) {
                    break;
                }
                j++;
            }

            monthlyDates.add(sortedDatesList[i]);

            i = j;
        }

        // skip the first entry
        if (monthlyDates.size() > 0) {
            monthlyDates.remove(0);
        }

        return monthlyDates;
    }

    public static List<LocalDate> getYearlyDates(Collection<LocalDate> sortedDates) {
        TreeMap<Integer, List<LocalDate>> januaryDatesPerYear = sortedDates.stream()
                .filter(date -> date.getMonth() == Month.JANUARY)
                .collect(Collectors.groupingBy(LocalDate::getYear,
                        TreeMap::new,
                        Collectors.mapping(Function.identity(), Collectors.toCollection(ArrayList::new))));

        List<LocalDate> firstDatesOfYears = new ArrayList<>();

        for (List<LocalDate> januaryDates : januaryDatesPerYear.values()) {
            firstDatesOfYears.add(Collections.min(januaryDates));
        }

        LocalDate firstDate = sortedDates.stream().findFirst().orElseThrow(IllegalStateException::new);

        if(firstDate.getMonth() == Month.JANUARY)
            firstDatesOfYears.remove(0);

        return firstDatesOfYears;
    }

}
