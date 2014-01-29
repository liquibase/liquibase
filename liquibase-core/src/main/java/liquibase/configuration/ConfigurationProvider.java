package liquibase.configuration;

public interface ConfigurationProvider {
    public Object getValue(String namespace, String property);

    String describeDefaultLookup(AbstractConfiguration.ConfigurationProperty property);
}
