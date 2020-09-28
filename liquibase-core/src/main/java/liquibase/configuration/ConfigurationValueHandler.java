package liquibase.configuration;

/**
 * Implementations can convert specified {@link ConfigurationProperty} values
 */
public interface ConfigurationValueHandler {

    Object convert(Object value);
}
