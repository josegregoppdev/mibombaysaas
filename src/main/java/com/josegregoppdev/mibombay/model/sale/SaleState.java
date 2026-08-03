package com.josegregoppdev.mibombay.model.sale;

public enum SaleState {
    EN_ESPERA("On Hold"),
    CONFIRMADA("Confirmed"),
    ANULADA("Cancelled");

    private final String displayName;

    SaleState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
