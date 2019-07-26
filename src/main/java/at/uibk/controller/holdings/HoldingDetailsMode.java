package at.uibk.controller.holdings;

public enum  HoldingDetailsMode {
    DIVIDEND_NEW,
    DIVIDEND_EDIT,
    TRADE_NEW,
    TRADE_EDIT;

    public boolean isDividend(){
        return this == DIVIDEND_NEW || this == DIVIDEND_EDIT;
    }

    public boolean isEdit(){
        return this == TRADE_EDIT || this == DIVIDEND_EDIT;
    }
}
