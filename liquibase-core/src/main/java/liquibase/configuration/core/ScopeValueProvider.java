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
    public ProvidedValue getProvidedValue(String... keyAndAliases) {
        if (keyAndAliases == null || keyAndAliases.length == 0) {
            return null;
        }

        for (String key : keyAndAliases) {
            final Object value = Scope.getCurrentScope().get(key, Object.class);
            if (value == null) {
                continue;
            }

            return new ProvidedValue(keyAndAliases[0], key, value, "Scoped value", this);
        }
        return null;
    }

    protected Properties getSystemProperties() {
        return System.getProperties();
    }

}
