package at.uibk.apis.stocks.alphaVantage.responseDataTypes;

import at.uibk.model.Currency;

public enum StockExchange {
//    BER("Börse Berlin", "Berlin", ".BER", ".BE", "BERA", Currency.EUR),
//    DUS("Börse Düsseldorf", "Dusseldorf", ".DUS", ".DU", "DUSA", Currency.EUR),
    FRK("Börse Frankfurt", "Frankfurt", ".FRK", ".F", "FRAA", Currency.EUR),
//    HAM("Börse Hamburg", "Hamburg", ".HAM", ".HM", "HAMA", Currency.EUR),
//    HAN("Börse Hannover", "Hanover", ".HAN", ".HA", "HANA", Currency.EUR),
//    MUN("Börse München", "Munich", ".MUN", ".MU", "MUNA", Currency.EUR),
//    STG("Börse Stuttgart", "Stuttgard", ".STG", ".SG", "STUA", Currency.EUR),
    VIE("Wiener Börse", "Vienna", ".VIE", ".VI", "WBAH", Currency.EUR),
    SWI("SIX Swiss Exchange", "Switzerland", ".SWI", ".SW", "XSWX", Currency.CHF),
    LON("London Stock Exchange", "United Kingdom", ".LON", ".L", "XLON", Currency.GBP),
//    US("US Markt", "United States", "", "", "XNAS", Currency.USD),
    NAS("Nasdaq", "United States", "", "", "XNAS", Currency.USD),
    NYS("New York Stock Exchange", "United States", "", "", "XNYS", Currency.USD);


    StockExchange(String name, String region, String extension, String searchExtension, String mic, Currency currency) {
        this.name = name;
        this.region = region;
        this.extension = extension;
        this.searchExtension = searchExtension;
        this.mic = mic;
        this.currency = currency;
    }

    public static final StockExchange DEFAULT = FRK;
    private String name;
    private String region;
    private String extension;
    private String searchExtension;
    private String mic;
    private Currency currency;

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public String getExtension() {
        return extension;
    }

    public String getSearchExtension() {
        return searchExtension;
    }

    public String getMic() {
        return mic;
    }

    public Currency getCurrency() {
        return currency;
    }

    private static String normalizeMIC(String mic){
        return mic.replace(" ", "").toUpperCase();
    }

    public static StockExchange ofMIC(String mic){
        if(mic == null || mic.isEmpty())
            throw new IllegalArgumentException("Null and empty strings are not allowed!");

        String normalizedMIC = normalizeMIC(mic);

        switch(normalizedMIC){
//            case "BERA": return BER;
//            case "DUSA": return DUS;
            case "FRAA": return FRK;
//            case "HAMA": return HAM;
//            case "HANA": return HAN;
//            case "MUNA": return MUN;
//            case "STUA": return STG;
            case "WBAH":
            case "WBDM":
                return VIE;
            case "XSWX": return SWI;
            case "XLON": return LON;
            case "XNAS": return NAS;
            case "XNYS": return NYS;
            default: throw new IllegalArgumentException("No matching MIC found!");
        }
    }

}
