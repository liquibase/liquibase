package liquibase.ui.interactive;


import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * This class exists to wrap the parameter enum so that individual usages can provide specific
 * overrides of the logic defined in the enum class. You should access the underlying parameter as little as possible
 * and instead, access it using getter wrapper methods in this class. That will allow future developers to specify
 * custom overrides for each rule (for example, overriding the default value for a specific usage without
 * affecting all others).
 */
public class InteractivePromptableCustomizationWrapper<T> {
    private final InteractivelyPromptableEnum parameter;
    private final BiFunction<T, Map<String, Object>, Boolean> validationCallbackOverride;
    private final Function<Map<String, Object>, Boolean> shouldPrompt;

    public InteractivePromptableCustomizationWrapper(InteractivelyPromptableEnum parameter) {
        this(parameter, null, null);
    }

    public InteractivePromptableCustomizationWrapper(InteractivelyPromptableEnum parameter, BiFunction<T, Map<String, Object>, Boolean> validationCallbackOverride, Function<Map<String, Object>, Boolean> shouldPrompt) {
        this.parameter = parameter;
        this.validationCallbackOverride = validationCallbackOverride;
        this.shouldPrompt = shouldPrompt;
    }

    public InteractivePromptableCustomizationWrapper(InteractivelyPromptableEnum parameter, Function<Map<String, Object>, Boolean> shouldPrompt) {
        this(parameter, null, shouldPrompt);
    }

    public Object getDefaultValue() {
        return parameter.getDefaultValue();
    }

    public InteractivelyPromptableEnum getParameter() {
        return parameter;
    }

    public AbstractCommandLineValueGetter<?> getInteractiveCommandLineValueGetter() {
        return parameter.getInteractiveCommandLineValueGetter();
    }

    public BiFunction<T, Map<String, Object>, Boolean> getValidationCallbackOverride() {
        return validationCallbackOverride;
    }

    public Function<Map<String, Object>, Boolean> getShouldPrompt() {
        return shouldPrompt;
    }

    @Override
    public String toString() {
        return getParameter().toString();
    }

    /**
     * Determine if the rule parameter should be prompted for in a checks customize scenario.
     * @param existingNewValues the values already provided in the checks customize session
     * @return true if the prompt should occur for this parameter, false if not
     */
    public boolean shouldPrompt(Map<String, Object> existingNewValues) {
        boolean resp = true;
        if (shouldPrompt != null) {
            resp = shouldPrompt.apply(existingNewValues);
        }
        return resp;
    }
}
