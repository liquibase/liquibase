package liquibase.configuration;

/**
 * A ConfigurationValueProvider implementation that looks for overriding values in system properties.
 * Looks for system properties in the format "NAMESPACE.PROPERTY_NAME".
 */
public class SystemPropertyProvider implements ConfigurationValueProvider {

    @Override
    public Object getValue(String namespace, String property) {
        return System.getProperty(namespace +"."+property);
    }

    @Override
    public String describeValueLookupLogic(ConfigurationProperty property) {
        return "System property '"+property.getNamespace()+"."+property.getName()+"'";
    }
}
