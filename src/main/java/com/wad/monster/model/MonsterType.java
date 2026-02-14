package com.wad.monster.model;

public enum MonsterType {
    FIRE("fire"),
    WATER("water"),
    WIND("wind");

    private final String value;

    MonsterType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
