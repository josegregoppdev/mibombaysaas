package com.josegregoppdev.mibombay.model.ingredient;

public enum Category {
    MEATS("Meats"),
    DAIRY("Dairy"),
    BREADS("Breads"),
    VEGETABLES("Vegetables"),
    FRUITS("Fruits"),
    GRAINS("Grains"),
    CONDIMENTS("Condiments"),
    BEVERAGES("Beverages"),
    OTHERS("Others");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
