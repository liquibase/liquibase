package liquibase.configuration.core;

import liquibase.configuration.AbstractMapConfigurationValueProvider;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This value provider handles values obtained during interactive CLI prompting.
 */
public class InteractivePromptingValueProvider extends AbstractMapConfigurationValueProvider {

    public static final SortedMap<String, Object> values = new TreeMap<>();

    @Override
    protected Map<?, ?> getMap() {
        return values;
    }

    @Override
    protected String getSourceDescription() {
        return "CLI interactive prompts";
    }

    @Override
    public int getPrecedence() {
        return 500;
    }
}
