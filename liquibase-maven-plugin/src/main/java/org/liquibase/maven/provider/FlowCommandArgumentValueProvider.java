package org.liquibase.maven.provider;

import liquibase.configuration.AbstractMapConfigurationValueProvider;

import java.util.Map;

public class FlowCommandArgumentValueProvider extends AbstractMapConfigurationValueProvider {

    private final Map<String, Object> args;

    public FlowCommandArgumentValueProvider(Map<String, Object> args) {
        this.args = args;
    }

    @Override
    public int getPrecedence() {
        return 250;
    }

    @Override
    protected Map<?, ?> getMap() {
        return args;
    }

    @Override
    protected String getSourceDescription() {
        return "Arguments provided through maven when invoking flow or flow.validate maven goals";
    }
}
