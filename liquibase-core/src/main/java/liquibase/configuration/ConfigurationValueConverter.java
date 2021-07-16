package liquibase.configuration;

import liquibase.Scope;

import java.util.logging.Level;

/**
 * Used by {@link ConfigurationDefinition#getCurrentConfiguredValue()} to translate whatever object type a {@link ConfigurationValueProvider} is returning
 * into the object type the definition uses.
 */
public interface ConfigurationValueConverter<DataType> {

    /**
     * Standard value -> java.util.logging.Level converter
     */
    ConfigurationValueConverter<Level> LOG_LEVEL = value -> {
        if (value == null) {
            return null;
        }
        if (value instanceof Level) {
            return (Level) value;
        }
        String stringLevel = String.valueOf(value).toUpperCase();
        if (stringLevel.equals("DEBUG")) {
            return Level.FINE;
        } else if (stringLevel.equals("WARN")) {
            return Level.WARNING;
        } else if (stringLevel.equals("ERROR")) {
            return Level.SEVERE;
        }

        try {
            return Level.parse(stringLevel);
        } catch (IllegalArgumentException e) {
            Scope.getCurrentScope().getUI().sendErrorMessage("WARNING:  Unknown log level " + stringLevel);
            return Level.INFO;
        }
    };

    /**
     * Converts an arbitrary object into the correct type.
     * Implementations should be able to handle <b>any</b> type passed them, often types by calling toString() on the incoming value and parsing the string.
     * Normally, a null value will be returned as a null value, but that is up to the implementation.
     *
     * @throws IllegalArgumentException if the value cannot be parsed or is an invalid value.
     */
    DataType convert(Object value) throws IllegalArgumentException;
}
