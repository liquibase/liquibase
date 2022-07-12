package liquibase.configuration.core;

import liquibase.configuration.AbstractConfigurationValueProvider;
import liquibase.configuration.AbstractMapConfigurationValueProvider;
import liquibase.configuration.ConfigurationValueProvider;
import liquibase.configuration.ProvidedValue;
import liquibase.util.StringUtil;

import java.util.Map;
import java.util.Properties;

/**
 * Searches for the configuration values in the system properties {@link System#getProperties()}.
 * <p>
 * To improve usability, it will search for the given key case insensitively.
 */
public class SystemPropertyValueProvider extends AbstractMapConfigurationValueProvider {

    @Override
    public int getPrecedence() {
        return 200;
    }

    @Override
    protected String getSourceDescription() {
        return "System property";
    }

    @Override
    protected Map<?, ?> getMap() {
        return System.getProperties();
    }
}
