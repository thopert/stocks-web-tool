package at.uibk.model.mainEntities;

public enum StockPriceAlarmType {
    UPPER_LIMIT("Obergrenze"),
    LOWER_LIMIT("Untergrenze"),
    UPPER_MOVE("Aufwärtsbewegung"),
    UPPER_PERCENT_MOVE("Aufwärtsbewegung %"),
    DOWN_MOVE("Abwärtsbewegung"),
    DOWN_PERCENT_MOVE("Abwärtsbewegung %"),
    PERCENT_INTERVAL("Schwankungsbereich %");

    StockPriceAlarmType(String label) {
        this.label = label;
    }

    private String label;

    public String getLabel() {
        return label;
    }

    public boolean isPercent(){
        return (this == PERCENT_INTERVAL || this == UPPER_PERCENT_MOVE || this == DOWN_PERCENT_MOVE);
    }
}
