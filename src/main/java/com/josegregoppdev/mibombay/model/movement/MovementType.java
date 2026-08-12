package com.josegregoppdev.mibombay.model.movement;

public enum MovementType {
    PURCHASE("Purchase"),
    SALE("Sale"),
    SHRINKAGE("Shrinkage"),
    ADJUSTMENT("Adjustment"),
    INITIAL_STOCK("Initial stock"),
    RETURN("Return");

    private final String displayName;

    MovementType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
