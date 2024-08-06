package com.example.liquibase.change;

import liquibase.serializer.AbstractLiquibaseSerializable;
import lombok.Getter;

@Getter
public class ColumnConfigExample extends AbstractLiquibaseSerializable {
    private String name;
    private String type;
    private Boolean nullable;
    private IdentityConfig identity;
    private DefaultConstraintConfig defaultConstraint;
    private ComputedConfig computed;

    @Override
    public String getSerializedObjectNamespace() {
        return GENERIC_CHANGELOG_EXTENSION_NAMESPACE;
    }

    @Override
    public String getSerializedObjectName() {
        return "column";
    }

    public ColumnConfigExample setName(String name) {
        this.name = name;
        return this;
    }

    public ColumnConfigExample setType(String type) {
        this.type = type;
        return this;
    }

    public ColumnConfigExample setNullable(Boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public ColumnConfigExample setIdentity(IdentityConfig identity) {
        this.identity = identity;
        return this;
    }

    public ColumnConfigExample setDefaultConstraint(DefaultConstraintConfig defaultConstraint) {
        this.defaultConstraint = defaultConstraint;
        return this;
    }

    public ColumnConfigExample setComputed(ComputedConfig computed) {
        this.computed = computed;
        return this;
    }
}
