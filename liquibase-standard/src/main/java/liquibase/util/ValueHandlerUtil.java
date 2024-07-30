package liquibase.util;

import java.util.List;
import java.util.stream.Collectors;

public class ValueHandlerUtil {

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
