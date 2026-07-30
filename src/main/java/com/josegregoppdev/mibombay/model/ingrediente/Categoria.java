package com.josegregoppdev.mibombay.model.ingrediente;

public enum Categoria {
    CARNES("Carnes"),
    LACTEOS("Lácteos"),
    PANES("Panes"),
    VERDURAS("Verduras"),
    FRUTAS("Frutas"),
    GRANOS("Granos"),
    CONDIMENTOS("Condimentos"),
    BEBIDAS("Bebidas"),
    OTROS("Otros");


    private final String displayName;

    Categoria(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
