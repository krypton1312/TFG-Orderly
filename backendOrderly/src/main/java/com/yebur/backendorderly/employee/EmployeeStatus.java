package com.yebur.backendorderly.employee;

public enum EmployeeStatus {
    ACTIVE("ACTIVO"),
    INACTIVE("INACTIVO"),
    SUSPENDED("SUSPENDIDO");

    private final String spanishName;

    EmployeeStatus(String spanishName) {
        this.spanishName = spanishName;
    }

    public String getSpanishName() {
        return spanishName;
    }
}
