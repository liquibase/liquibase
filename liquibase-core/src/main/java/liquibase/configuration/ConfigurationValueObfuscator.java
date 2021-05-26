package liquibase.configuration;

/**
 * Used by {@link ConfigurationDefinition#getCurrentValueObfuscated()} to obfuscate the current value.
 */
public interface ConfigurationValueObfuscator<DataType> {

    /**
     * Return an "obfuscated" version of the given value, suitable for logging or storing in non-secure environments.
     */
    DataType obfuscate(DataType value);
}
