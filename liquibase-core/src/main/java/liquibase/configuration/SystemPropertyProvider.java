package liquibase.configuration;

import liquibase.util.StringUtil;

import java.util.Map;
import java.util.Properties;

/**
 * A ConfigurationValueProvider implementation that looks for overriding values in system properties.
 * Looks for system properties in the format "NAMESPACE.PROPERTY_NAME".
 */
public class SystemPropertyProvider implements ConfigurationValueProvider {

    @Override
    public Object getValue(String namespace, String property) {
        String propValue = System.getProperty(namespace +"."+property);
        if (StringUtil.isNotEmpty(propValue)) {
            return propValue;
        }

        //
        // Not matching with the actual key then try case insensitive
        //
        Properties properties = System.getProperties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String)entry.getKey();
            if (key.equalsIgnoreCase(namespace + "." + property)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public String describeValueLookupLogic(ConfigurationProperty property) {
        return "System property '"+property.getNamespace()+"."+property.getName()+"'";
    }
}
