package liquibase.configuration.core;

import liquibase.configuration.AbstractMapConfigurationValueProvider;
import liquibase.configuration.ConfigurationDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link liquibase.configuration.ConfigurationValueProvider} that simulates the behavior from pre-4.4 methods like {@link liquibase.configuration.GlobalConfiguration#setOutputEncoding(String)}.
 * It is also useful for integrations that do not yet support a "Scope" style calling of logic.
 * The values set in here should override environmental settings like {@link SystemPropertyValueProvider} but is overridden by new-style code using {@link ScopeValueProvider}.
 *
 * @deprecated
 */
public class DeprecatedConfigurationValueProvider extends AbstractMapConfigurationValueProvider {

    private static final Map<String, Object> data = new HashMap<>();

    @Override
    protected Map<?, ?> getMap() {
        return data;
    }

    @Override
    public int getPrecedence() {
        return 350;
    }

    /**
     * @deprecated
     */
    public static void setData(String key, Object value) {
        data.put(key, value);
    }

    /**
     * @deprecated
     */
    public static <T> void setData(ConfigurationDefinition<T> configuration, T value) {
        data.put(configuration.getKey(), value);
    }

    /**
     * Clears all data stored in this provider.
     * @deprecated
     */
    public static void clearData() {
        data.clear();
    }

    @Override
    protected String getSourceDescription() {
        return "Legacy configuration";
    }
}
