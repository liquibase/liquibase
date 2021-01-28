package liquibase.configuration.core;

import liquibase.configuration.ConfigurationValueProvider;
import liquibase.util.StringUtil;

import java.util.Map;
import java.util.Properties;

/**
 * A ConfigurationValueProvider implementation that looks for overriding values in system properties.
 * Looks for system properties in the format "NAMESPACE.PROPERTY_NAME".
 */
public class SystemPropertyProvider implements ConfigurationValueProvider {

    @Override
    public int getPrecedence() {
        return 20;
    }

    @Override
    public Object getValue(String property) {
        String propValue = System.getProperty(property);
        if (StringUtil.isNotEmpty(propValue)) {
            return propValue;
        }

        //
        // Not matching with the actual key then try case insensitive
        //
        Properties properties = System.getProperties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.equalsIgnoreCase(property)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public String describeValueLookupLogic(String property) {
        return "System property '" + property + "'";
    }
}
