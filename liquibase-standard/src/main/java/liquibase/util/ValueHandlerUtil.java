package liquibase.util;

import java.util.Arrays;

public class ValueHandlerUtil {
    /**
     * Get the valid enum value from a configuration parameter if possible.
     *
     * @param enumClass     the enum to use
     * @param input         the configuration input to search the enumClass
     * @param parameterName the name to report to the user if no valid enum values are found
     * @return the enum value or null
     */
    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, Object input, String parameterName) {
        if (input == null) {
            return null;
        }
        if (input instanceof String) {
            String stringInput = (String) input;

            if (Arrays.stream(enumClass.getEnumConstants()).noneMatch(enumValue -> enumValue.toString().equalsIgnoreCase(stringInput))) {
                throw new IllegalArgumentException(String.format("WARNING: The %s value '%s' is not valid. Valid values include: '%s'",
                        parameterName.toLowerCase(),
                        stringInput,
                        StringUtil.join(enumClass.getEnumConstants(), "', '", Object::toString)));
            }
            return Enum.valueOf(enumClass, stringInput.toUpperCase());
        } else if (enumClass.isAssignableFrom(input.getClass())) {
            return enumClass.cast(input);
        } else {
            return null;
        }
    }

    public static Boolean booleanValueHandler(Object input) {
        if (input == null) {
            return true;
        }
        if (input instanceof Boolean) {
            return (Boolean) input;
        }
        String verboseString = (String) input;
        if (verboseString.equalsIgnoreCase("true") || verboseString.equalsIgnoreCase("false")) {
            return Boolean.valueOf(verboseString);
        }
        String messageString =
                "\nWARNING:  The input '" + verboseString + "' is not valid.  Options: 'true' or 'false'.";
        throw new IllegalArgumentException(messageString);
    }
}
