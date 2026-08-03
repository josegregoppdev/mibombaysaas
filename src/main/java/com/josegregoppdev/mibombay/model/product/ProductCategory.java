package com.josegregoppdev.mibombay.model.product;

public enum ProductCategory {
    FOOD("Food"),
    DRINKS("Drinks"),
    DESSERTS("Desserts"),
    SIDES("Sides"),
    COMBOS("Combos"),
    OTHERS("Others");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
