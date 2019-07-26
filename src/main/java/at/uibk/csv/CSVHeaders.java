package at.uibk.csv;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class CSVHeaders implements Serializable {
    private List<CSVHeader> csvHeaders;
    private Collection<String> headerNames;

    private CSVHeaders(List<CSVHeader> csvHeaders, Collection<String> headerNames) {
        this.csvHeaders = new ArrayList<>(csvHeaders);
        this.headerNames = headerNames;
    }

    public CSVHeaders(List<CSVHeader> csvHeaders) {
        this.csvHeaders = csvHeaders;
    }

    public static CSVHeaders of(Collection<String> headerNames) {
        Map<CSVHeaderType, String> estimateMap = new HashMap<>();

        for (String headerToCheck : headerNames) {
            CSVHeaderType csvHeaderType = CSVHeaderType.estimateOf(headerToCheck);
            if (csvHeaderType != null) {
                estimateMap.put(csvHeaderType, headerToCheck);
            }
        }

        List<CSVHeader> csvHeaders = Arrays.stream(CSVHeaderType.values())
                .map(csvHeaderType -> new CSVHeader(csvHeaderType, estimateMap.get(csvHeaderType)))
                .collect(Collectors.toList());

        return new CSVHeaders(csvHeaders, headerNames);
    }

    public String getName(CSVHeaderType csvHeaderType) {
        CSVHeader csvHeader = getHeader(csvHeaderType);
        return csvHeader.getName();
    }

    public CSVHeader getHeader(CSVHeaderType csvHeaderType) {
        for (CSVHeader csvHeader : csvHeaders) {
            if (csvHeader.getType() == csvHeaderType) {
                return csvHeader;
            }
        }
        return null; // cannot happen -> all header types are present in list
    }

    public boolean isSet(CSVHeaderType csvHeaderType) {
        return getName(csvHeaderType) != null;
    }

    public Collection<CSVHeader> getHeaders() {
        return getHeaderTypesOrdered().stream()
                .map(this::getHeader)
                .collect(Collectors.toList());
    }

    public Collection<String> getSelectableNames() {
        return this.headerNames;
    }

    public boolean obligatoryHeadersSet() {
        return csvHeaders.stream()
                .filter(csvHeader -> csvHeader.getType().isObligatory())
                .map(CSVHeader::getName)
                .allMatch(Objects::nonNull);
    }

    private static List<CSVHeaderType> getHeaderTypesOrdered() {
        return Arrays.asList(
                CSVHeaderType.SYMBOL,
                CSVHeaderType.MIC,
                CSVHeaderType.DATE,
                CSVHeaderType.TYPE,
                CSVHeaderType.PRICE,
                CSVHeaderType.QUANTITY,
                CSVHeaderType.EXCHANGE_RATE,
//                CSVHeaderType.EXCHANGE_RATE_DIRECTION,
                CSVHeaderType.BROKERAGE,
                CSVHeaderType.BROKERAGE_CURRENCY);
    }

    @Override
    public String toString() {
        return csvHeaders.toString();
    }
}
