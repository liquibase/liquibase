package liquibase.util;

import liquibase.Scope;
import liquibase.configuration.ConfigurationDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ValueHandlerUtil {

    public static String ARGUMENT_KEY = "key";

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
        String key = Scope.getCurrentScope().get(ARGUMENT_KEY, String.class);
        String messageString;
        if (key != null) {
            messageString = "\nWARNING:  The input for '" + key + "' is '" + verboseString + "', which is not valid.  " +
                 "Options: 'true' or 'false'.";
        } else {
            messageString = "\nWARNING:  The input '" + verboseString + "' is not valid.  Options: 'true' or 'false'.";
        }
        throw new IllegalArgumentException(messageString);
    }

    /**
     * Get an integer entry, with constraints.
     * @param input the user supplied input
     * @param errorMessage the error message that should be returned if none of the valid values match. This message
     *                     should end with the string "one of the allowed values: ", because the allowed values will
     *                     be appended to this error message before it is used.
     * @param validValues the permissible values for the input
     */
    public static Integer getIntegerWithConstraints(Object input, String errorMessage, List<Integer> validValues) {
        if (input == null) {
            return null;
        }

        Integer convertedInput = Integer.valueOf(String.valueOf(input));

        boolean anyMatch = validValues.contains(convertedInput);
        if (!anyMatch) {
            throw new IllegalArgumentException(errorMessage + StringUtil.join(validValues.stream().sorted().map(String::valueOf).collect(Collectors.toList()), ", "));
        }
        return convertedInput;
    }
}
