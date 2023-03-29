package com.example.liquibase.change;

import liquibase.serializer.AbstractLiquibaseSerializable;

public class ComputedConfig extends AbstractLiquibaseSerializable {
    private String expression;
    private Boolean persisted;

    @Override
    public String getSerializedObjectNamespace() {
        return GENERIC_CHANGELOG_EXTENSION_NAMESPACE;
    }

    @Override
    public String getSerializedObjectName() {
        return "computed";
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        if ("expression".equals(field)) {
            return SerializationType.DIRECT_VALUE;
        }
        return SerializationType.NAMED_FIELD;
    }

    public String getExpression() {
        return expression;
    }

    public ComputedConfig setExpression(String expression) {
        this.expression = expression;
        return this;
    }

    public Boolean getPersisted() {
        return persisted;
    }

    public ComputedConfig setPersisted(Boolean persisted) {
        this.persisted = persisted;
        return this;
    }
}
