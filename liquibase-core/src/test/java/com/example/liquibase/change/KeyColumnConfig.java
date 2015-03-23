package com.example.liquibase.change;

public class KeyColumnConfig extends ColumnConfig {
    private Boolean descending;

    @Override
    public String getSerializedObjectName() {
        return "keyColumn";
    }

    public Boolean getDescending() {
        return descending;
    }

    public void setDescending(Boolean descending) {
        this.descending = descending;
    }
}
