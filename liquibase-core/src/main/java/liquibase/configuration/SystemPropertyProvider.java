package liquibase.configuration;

public class SystemPropertyProvider implements ConfigurationProvider {

    @Override
    public Object getValue(String namespace, String property) {
        return System.getProperty(namespace +"."+property);
    }

    @Override
    public String describeDefaultLookup(ConfigurationProperty property) {
        return "System property '"+property.getNamespace()+"."+property.getName()+"'";
    }
}
