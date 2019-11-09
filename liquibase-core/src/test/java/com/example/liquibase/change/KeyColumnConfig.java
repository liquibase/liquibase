package com.example.liquibase.change;

import liquibase.serializer.AbstractLiquibaseSerializable;

public class KeyColumnConfig extends AbstractLiquibaseSerializable {
    private String name;
    private Boolean descending;

    @Override
    public String getSerializedObjectNamespace() {
        return GENERIC_CHANGELOG_EXTENSION_NAMESPACE;
    }

    @Override
    public String getSerializedObjectName() {
        return "keyColumn";
    }

    public String getName() {
        return name;
    }

    public KeyColumnConfig setName(String name) {
        this.name = name;
        return this;
    }

    public Boolean getDescending() {
        return descending;
    }

    public KeyColumnConfig setDescending(Boolean descending) {
        this.descending = descending;
        return this;
    }
}
