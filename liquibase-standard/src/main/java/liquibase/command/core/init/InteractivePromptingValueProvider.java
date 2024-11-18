package liquibase.command.core.init;

import liquibase.configuration.AbstractMapConfigurationValueProvider;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This value provider handles values obtained during interactive CLI prompting.
 */
public class InteractivePromptingValueProvider extends AbstractMapConfigurationValueProvider {

    private final SortedMap<String, Object> values = new TreeMap<>();

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

    public void addValue(String key, Object value) {
        values.put(key, value);
    }
}