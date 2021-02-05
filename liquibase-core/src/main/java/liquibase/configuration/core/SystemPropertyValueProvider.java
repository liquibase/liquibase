package liquibase.configuration.core;

import liquibase.configuration.ConfigurationValueProvider;
import liquibase.configuration.CurrentValueSourceDetails;
import liquibase.util.StringUtil;

import java.util.Map;
import java.util.Properties;

/**
 * Searches for the configuration values in the system properties {@link System#getProperties()}.
 * <p>
 * To improve usability, it will search for the given key case insensitively.
 */
public class SystemPropertyValueProvider implements ConfigurationValueProvider {

    @Override
    public int getPrecedence() {
        return 20;
    }

    @Override
    public CurrentValueSourceDetails getValue(String key) {
        if (key == null) {
            return null;
        }

        final Properties systemProperties = getSystemProperties();

        String propValue = systemProperties.getProperty(key);
        if (StringUtil.isNotEmpty(propValue)) {
            return new CurrentValueSourceDetails(propValue, "System property", key);
        }

        //
        // Not matching with the actual key then try case insensitive
        //
        for (Map.Entry<Object, Object> entry : systemProperties.entrySet()) {
            String foundKey = (String) entry.getKey();
            if (foundKey.equalsIgnoreCase(key)) {
                return new CurrentValueSourceDetails(entry.getValue(),"System property", foundKey);
            }
        }
        return null;
    }

    protected Properties getSystemProperties() {
        return System.getProperties();
    }
}
