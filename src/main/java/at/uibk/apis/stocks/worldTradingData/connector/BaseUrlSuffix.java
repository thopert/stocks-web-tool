package at.uibk.apis.stocks.worldTradingData.connector;

public enum BaseUrlSuffix {
    STOCK("stock"), HISTORY("history");

    private String name;

    public String getName() {
        return name;
    }

    BaseUrlSuffix(String name) {
        this.name = name;
    }
}
