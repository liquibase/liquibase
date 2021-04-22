package liquibase.configuration.core;

import liquibase.Scope;
import liquibase.configuration.AbstractConfigurationValueProvider;
import liquibase.configuration.ProvidedValue;

import java.util.Properties;

/**
 * Searches the {@link liquibase.Scope} for the given key.
 * Does not perform any key smoothing/translating.
 */
public class ScopeValueProvider extends AbstractConfigurationValueProvider {

    @Override
    public int getPrecedence() {
        return 400;
    }

    @Override
    public ProvidedValue getProvidedValue(String key) {
        if (key == null) {
            return null;
        }

        final Object value = Scope.getCurrentScope().get(key, Object.class);
        if (value == null) {
            return null;
        }

        return new ProvidedValue(key, key, value, "Scoped value", this);
    }

    protected Properties getSystemProperties() {
        return System.getProperties();
    }

}
