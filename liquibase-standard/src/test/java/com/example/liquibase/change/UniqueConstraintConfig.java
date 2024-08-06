package com.example.liquibase.change;

import liquibase.serializer.AbstractLiquibaseSerializable;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class UniqueConstraintConfig extends AbstractLiquibaseSerializable {
    private String name;
    private List<KeyColumnConfig> keyColumns = new ArrayList<KeyColumnConfig>();

    @Override
    public String getSerializedObjectNamespace() {
        return GENERIC_CHANGELOG_EXTENSION_NAMESPACE;
    }

    @Override
    public String getSerializedObjectName() {
        return "uniqueConstraint";
    }

    public UniqueConstraintConfig setName(String name) {
        this.name = name;
        return this;
    }

    public UniqueConstraintConfig setKeyColumns(List<KeyColumnConfig> keyColumns) {
        this.keyColumns = keyColumns;
        return this;
    }
}
