package com.example.liquibase.change;

import liquibase.serializer.AbstractLiquibaseSerializable;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class IdentityConfig extends AbstractLiquibaseSerializable {
    private BigDecimal seed;
    private BigDecimal increment;

    @Override
    public String getSerializedObjectNamespace() {
        return GENERIC_CHANGELOG_EXTENSION_NAMESPACE;
    }

    @Override
    public String getSerializedObjectName() {
        return "identity";
    }

    public IdentityConfig setSeed(BigDecimal seed) {
        this.seed = seed;
        return this;
    }

    public IdentityConfig setIncrement(BigDecimal increment) {
        this.increment = increment;
        return this;
    }
}
