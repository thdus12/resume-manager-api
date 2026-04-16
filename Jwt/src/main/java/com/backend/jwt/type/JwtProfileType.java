package com.backend.jwt.type;

import lombok.Getter;

@Getter
public enum JwtProfileType {
    ADMIN("admin"),
    USER("user");

    private final String desc;

    JwtProfileType(String desc) {
        this.desc = desc;
    }

    public static JwtProfileType fromName(String text) {
        for (JwtProfileType value : JwtProfileType.values()) {
            if (value.name().equalsIgnoreCase(text)) {
                return value;
            }
        }
        return null;
    }
}