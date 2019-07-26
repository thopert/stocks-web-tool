package at.uibk.apis.exchangeRates.parsing;

enum JsonKeys {
    ERROR("error"),
    BASE("base"),
    DATE("date"),
    RATES("rates"),
    START("start_at"),
    END("end_at");

    private String value;

    public String getValue() {
        return value;
    }

    JsonKeys(String value){
        this.value = value;
    }
}
