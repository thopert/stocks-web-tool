package at.uibk.charts.utils;

public enum StockIndex {
    ATX("^ATX", "ATX", "Austrian Traded Index"),
    DAX("^GDAXI", "DAX", "Deutscher Aktienindex"),
    MDAX("^MDAXI", "MDAX", "Mid-Cap-DAX"),
    DJI("^DJI", "DJI", "Dow Jones Industrial Average"),
    SP500("^GSPC", "S&P 500", "Standard & Poor's 500"),
    NIKKEI ("^N225", "N225", "Nikkei 225"),
    EURO_STOXX_50 ("^STOXX50E", "STOXX50E", "Euro Stoxx 50"),
    CAC_40 ("^FCHI", "CAC 40", "French Market Index"),
    FTSE_100 ("^FTSE", "FTSE 100", "Financial Times Stock Exchange Index");

    private String fullyQualifiedSymbol;

    private String abbreviation;

    private String name;

    StockIndex(String fullyQualifiedSymbol, String abbreviation, String name) {
        this.fullyQualifiedSymbol = fullyQualifiedSymbol;
        this.abbreviation = abbreviation;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getFullyQualifiedSymbol() {
        return fullyQualifiedSymbol;
    }
}
