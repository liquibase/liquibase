package liquibase.configuration.core;

import liquibase.configuration.AbstractMapConfigurationValueProvider;

import java.util.Map;

public class MapConfigurationValueProvider extends AbstractMapConfigurationValueProvider {

    private final Map<?, ?> map;


    public MapConfigurationValueProvider(Map<?, ?> map) {
        this.map = map;
    }

    @Override
    public int getPrecedence() {
        return -1;
    }

    @Override
    protected String getSourceDescription() {
        return "In memory map";
    }

    @Override
    protected Map<?, ?> getMap() {
        return map;
    }
}
