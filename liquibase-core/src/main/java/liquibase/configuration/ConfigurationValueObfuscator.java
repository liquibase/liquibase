package liquibase.configuration;

/**
 * Used by {@link ConfigurationDefinition#getCurrentValueObfuscated()} to obfuscate the current value.
 */
public interface ConfigurationValueObfuscator<DataType> {

    /**
     * Standard obfuscator. Returns the constant "*****".
     */
    ConfigurationValueObfuscator<String> STANDARD = value -> value == null ? null : "*****";

    /**
     * Not really an obfuscator -- simply returns the passed value directly.
     * Used for times the code wants to explicitly say "I have no obfuscator"
     */
    ConfigurationValueObfuscator<String> NONE = value -> value;

    /**
     * Return an "obfuscated" version of the given value, suitable for logging or storing in non-secure environments.
     */
    DataType obfuscate(DataType value);
}
