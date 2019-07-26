package at.uibk.csv;

public enum CSVHeaderType {
    DATE,
    SYMBOL,
    MIC,
    QUANTITY,
    PRICE,
    TYPE,
    EXCHANGE_RATE,
//    EXCHANGE_RATE_DIRECTION,
    BROKERAGE,
    BROKERAGE_CURRENCY;

    private static String normalize(String header) {
        return header.replace(" ", "").toLowerCase();
    }

    public boolean isObligatory() {
        return !(this == EXCHANGE_RATE ||
                this == BROKERAGE ||
                this == BROKERAGE_CURRENCY );
    }

    public static CSVHeaderType estimateOf(String header) {
        String normalizedHeader = normalize(header);

        if (normalizedHeader.contains("date") || normalizedHeader.contains("time")) {
            return CSVHeaderType.DATE;
        }
        if (normalizedHeader.contains("symbol") || normalizedHeader.contains("ticker") ||
                normalizedHeader.contains("code")) {
            return CSVHeaderType.SYMBOL;
        }
        if (normalizedHeader.contains("price")) {
            return CSVHeaderType.PRICE;
        }
        if (normalizedHeader.contains("quantity") || normalizedHeader.contains("shares")
                || normalizedHeader.contains("number")) {
            return CSVHeaderType.QUANTITY;
        }
        if (normalizedHeader.contains("type")) {
            return CSVHeaderType.TYPE;
        }
        if (normalizedHeader.contains("rate")) {
            return CSVHeaderType.EXCHANGE_RATE;
        }
        if (normalizedHeader.contains("mic") || normalizedHeader.contains("exchange") ||
                normalizedHeader.contains("market")) {
            return CSVHeaderType.MIC;
        }
        if (normalizedHeader.contains("currency")) {
            return CSVHeaderType.BROKERAGE_CURRENCY;
        }
        if (normalizedHeader.contains("brokerage")) {
            return CSVHeaderType.BROKERAGE;
        }
//        if (normalizedHeader.contains("type"))
//            return CSVHeaderType.EXCHANGE_RATE_DIRECTION;

        return null;
    }
}