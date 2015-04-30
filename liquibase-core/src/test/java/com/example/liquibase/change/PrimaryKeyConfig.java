package com.example.liquibase.change;

import java.util.ArrayList;
import java.util.List;

import liquibase.serializer.AbstractLiquibaseSerializable;

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

    public void setName(String name) {
        this.name = name;
    }

    public List<KeyColumnConfig> getKeyColumns() {
        return keyColumns;
    }

    public void setKeyColumns(List<KeyColumnConfig> keyColumns) {
        this.keyColumns = keyColumns;
    }
}
