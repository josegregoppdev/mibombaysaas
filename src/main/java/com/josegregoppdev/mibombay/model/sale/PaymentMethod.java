package com.josegregoppdev.mibombay.model.sale;

public enum PaymentMethod {
    EFECTIVO("Cash"),
    DATAFONO("Card"),
    TRANSFERENCIA("Transfer");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
