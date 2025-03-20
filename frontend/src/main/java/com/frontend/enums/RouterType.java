package com.frontend.enums;

public enum RouterType {

    LIGHT("燈光"),
    DESKLAMP("桌燈"),
    AIRCONDITIONER("空調"),
    CAMERA("攝影機");

    private final String displayName;

    RouterType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RouterType fromString(String type) {
        for (RouterType routerType : RouterType.values()) {
            if (routerType.name().equalsIgnoreCase(type)) {
                return routerType;
            }
        }
        throw new IllegalArgumentException("Unsupported router type: " + type);
    }
}
