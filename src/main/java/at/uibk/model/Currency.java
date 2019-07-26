package at.uibk.model;

public enum Currency {
    CHF("Fr", "CHF"),
    EUR ("\u20AC", "EUR"),
    GBP("\u00A3", "GBP"),
    USD("US\u0024", "USD");

    public static final Currency DEFAULT = EUR;
    private String symbol;
    private String code;

    Currency(String symbol, String code) {
        this.symbol = symbol;
        this.code = code;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCode() {
        return code;
    }

    public static void main(String[] args) {
        System.out.println(Currency.EUR);
    }
}
