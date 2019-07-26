package at.uibk.model.mainEntities;

public enum TradeType {

    BUY, SELL, SPLIT, CONSOLIDATION;

    public static final TradeType DEFAULT = BUY;

    public boolean isTrade() {
        return this == BUY || this == SELL;
    }

    public boolean isAdjustment() {
        return this == SPLIT || this == CONSOLIDATION;
    }

    public boolean isReduction() {
        return this == SELL || this == CONSOLIDATION;
    }

}
