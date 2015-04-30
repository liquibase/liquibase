package com.example.liquibase.change;

import liquibase.serializer.AbstractLiquibaseSerializable;

public class ColumnConfig extends AbstractLiquibaseSerializable {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getNullable() {
        return nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    public IdentityConfig getIdentity() {
        return identity;
    }

    public void setIdentity(IdentityConfig identity) {
        this.identity = identity;
    }

    public DefaultConstraintConfig getDefaultConstraint() {
        return defaultConstraint;
    }

    public void setDefaultConstraint(DefaultConstraintConfig defaultConstraint) {
        this.defaultConstraint = defaultConstraint;
    }

    public ComputedConfig getComputed() {
        return computed;
    }

    public void setComputed(ComputedConfig computed) {
        this.computed = computed;
    }
}
