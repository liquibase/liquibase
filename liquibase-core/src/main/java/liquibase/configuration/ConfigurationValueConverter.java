package liquibase.configuration;

/**
 * Used by {@link ConfigurationDefinition#getCurrentConfiguredValue()} to translate whatever object type a {@link ConfigurationValueProvider} is returning
 * into the object type the definition uses.
 */
public interface ConfigurationValueConverter<DataType> {

    /**
     * Converts an arbitrary object into the correct type.
     * Implementations should be able to handle <b>any</b> type passed them, often types by calling toString() on the incoming value and parsing the string.
     * Normally, a null value will be returned as a null value, but that is up to the implementation.
     *
     * @throws IllegalArgumentException if the value cannot be parsed or is an invalid value.
     */
    DataType convert(Object value) throws IllegalArgumentException;
}
