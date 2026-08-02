package com.josegregoppdev.mibombay.model.product;

public enum ProductType {
    CON_RECETA("With recipe"),
    SIN_RECETA("Without recipe"),
    ADICIONAL("Add-on");

    private final String displayName;

    ProductType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
