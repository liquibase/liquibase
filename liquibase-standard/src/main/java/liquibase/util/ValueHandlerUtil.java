package liquibase.util;

import liquibase.Scope;

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
}
