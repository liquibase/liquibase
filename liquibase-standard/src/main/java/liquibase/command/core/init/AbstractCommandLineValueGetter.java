package liquibase.command.core.init;

import liquibase.Scope;
import liquibase.ui.InputHandler;
import liquibase.ui.UIService;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * This class represents the basis for prompts to the user to input values, and should be used as part of the process
 * of obtaining user input for quality checks.
 * @param <T> the type the user is expected to enter
 */
@Getter
public abstract class AbstractCommandLineValueGetter<T> {

    public static final String NEW_PARAMETER_VALUES_SCOPE_KEY = "newParameterValues";
    /**
     * The type of the value that will be obtained.
     */
    private final Class<T> clazz;

    /**
     * Create a new value getter.
     * @param clazz the type of the value that will be obtained
     */
    public AbstractCommandLineValueGetter(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Prompt the user to enter a value for a parameter, and include the existing value in the prompt.
     * @param parameter the parameter to obtain a value for
     * @param newParameterValues a list of the values that have already been entered by the user during this interactive CLI parameter prompting situation
     * @return the value entered by the user
     */
    public final T prompt(InitProjectPromptingEnum parameter, Object currentValue) throws Exception {
        // determine which value should be displayed in square brackets as the value that will be selected if user presses enter
        Object valueToPromptAsDefault;
        if (currentValue != null) {
            /*
             * The value is being converted here, because enum types get read from the check settings file as strings.
             * As a result, the default value and current value (which should be the same type), are not; the default
             * value would be an enum and the current value would be a string.
             */
            try {
                valueToPromptAsDefault = convert((String) currentValue);
            } catch (Exception e) {
                valueToPromptAsDefault = currentValue;
            }
        } else {
            valueToPromptAsDefault = parameter.defaultValue;
        }

        // assume the user just hit enter to accept the default and revalidate it, since hitting enter to accept the
        // default skips the validation logic in ConsoleUIService. Reprompt endlessly until a valid value is entered.
        T prompt = null;
        boolean valid = false;
        while (!valid && (prompt == null || prompt.equals(valueToPromptAsDefault))) {
            prompt = doPrompt(parameter, valueToPromptAsDefault, (currentValue != null || parameter.defaultValue != null)); // todo || parameter.allowEmpty()

            try {
                valid = doValidate(parameter, prompt);
            } catch (IllegalArgumentException e) {
                UIService uiService = Scope.getCurrentScope().getUI();
                if (uiService.getAllowPrompt()) {
                    uiService.sendErrorMessage("Invalid value: '" + prompt + "': " + e.getMessage());
                } else {
                    /*
                    If the UIService does not allow prompting, and the value provided is invalid, then we know that the
                    default value provided is not valid, and it is therefore impossible to obtain a valid value and
                    so we should exit here.
                     */
                    throw e;
                }
            }
        }
        return prompt;
    }

    private T doPrompt(InitProjectPromptingEnum parameter, Object valueToPromptAsDefault, boolean shouldAllowEmptyValues) {
        return Scope.getCurrentScope().getUI().prompt(getMessage(parameter), (T) valueToPromptAsDefault, new InputHandler<T>() {
            @Override
            public T parseInput(String input, Class<T> type) throws IllegalArgumentException {
                T convert;
                try {
                    convert = AbstractCommandLineValueGetter.this.convert(input);
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        throw new IllegalArgumentException(
                                String.format("Invalid value: '%s': %s", input, e.getMessage()), e);
                    }
                    throw new IllegalArgumentException(e);
                }

                try {
                    if (!doValidate(parameter, convert)) {
                        throw new IllegalArgumentException("The supplied value is not valid.");
                    }
                } catch (Exception e) {
                    if (e.getMessage() != null) {
                        throw new IllegalArgumentException(
                                String.format("Invalid value: '%s': %s", input, e.getMessage()), e);
                    }
                    throw new IllegalArgumentException(
                            String.format("Invalid value: '%s': The supplied value is not valid.", input), e);
                }
                return convert;
            }

            @Override
            public boolean shouldAllowEmptyInput() {
                // We do not allow empty input, because as of right now, all parameters require values. In the future,
                // there may be parameters which permit empty values, which would need to tie in here.
                return shouldAllowEmptyValues;
            }
        }, clazz);
    }

    private boolean doValidate(InitProjectPromptingEnum parameter, T convert) throws IllegalArgumentException {
        return AbstractCommandLineValueGetter.this.validate(convert);
    }

    /**
     * Generate the prompt message.
     * @param parameter the parameter to prompt for
     * @return the message
     */
    private String getMessage(InitProjectPromptingEnum parameter) {
        String msg = parameter.uiMessage;
        if (StringUtils.isNotEmpty(parameter.options)) {
            msg += " (options: " + parameter.options + ")";
        }
        return msg;
    }

    /**
     * Given the input from the user, after being converted, validate it.
     * @return true if it is valid
     */
    public abstract boolean validate(T input);

    /**
     * Given the raw input string from the user, convert it to the right type.
     * @param input the raw input string from the prompt
     * @return the converted input
     */
    public abstract T convert(String input);

    /**
     * Describe the type of input expected by this class.
     */
    public String describe() {
        return getClass().getSimpleName();
    }
}
