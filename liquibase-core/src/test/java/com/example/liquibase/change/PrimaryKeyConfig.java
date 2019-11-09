package com.example.liquibase.change;

import liquibase.serializer.AbstractLiquibaseSerializable;

import java.util.ArrayList;
import java.util.List;

public class PrimaryKeyConfig extends AbstractLiquibaseSerializable {
    private String name;
    private List<KeyColumnConfig> keyColumns = new ArrayList<KeyColumnConfig>();

    @Override
    public String getSerializedObjectNamespace() {
        return GENERIC_CHANGELOG_EXTENSION_NAMESPACE;
    }

    @Override
    public String getSerializedObjectName() {
        return "primaryKey";
    }

    public String getName() {
        return name;
    }

    public PrimaryKeyConfig setName(String name) {
        this.name = name;
        return this;
    }

    public List<KeyColumnConfig> getKeyColumns() {
        return keyColumns;
    }

    public PrimaryKeyConfig setKeyColumns(List<KeyColumnConfig> keyColumns) {
        this.keyColumns = keyColumns;
        return this;
    }
}
