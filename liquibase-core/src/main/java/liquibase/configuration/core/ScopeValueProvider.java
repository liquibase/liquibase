package liquibase.configuration.core;

import liquibase.Scope;
import liquibase.configuration.ConfigurationValueProvider;
import liquibase.configuration.CurrentValueSourceDetails;

import java.util.Properties;

/**
 * Searches the {@link liquibase.Scope} for the given key.
 * Does not perform any key smoothing/translating.
 */
public class ScopeValueProvider implements ConfigurationValueProvider {

    @Override
    public int getPrecedence() {
        return 50;
    }

    @Override
    public CurrentValueSourceDetails getValue(String key) {
        if (key == null) {
            return null;
        }

        final Object value = Scope.getCurrentScope().get(key, Object.class);
        if (value == null) {
            return null;
        }

        return new CurrentValueSourceDetails(value, "Scoped value", key);
    }

    protected Properties getSystemProperties() {
        return System.getProperties();
    }

}
