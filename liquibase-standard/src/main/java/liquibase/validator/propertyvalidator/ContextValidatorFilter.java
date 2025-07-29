package liquibase.validator.propertyvalidator;

import liquibase.GlobalConfiguration;
import liquibase.validator.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;

/**
 * Validates the context property of a {@link RawChangeSet}. Ensures that the context is not empty when strict mode is enabled.
 * If the context is empty in strict mode, it won't accept the provided changeSet and will provide a validation error message.
 */
public class ContextValidatorFilter implements ValidatorFilter {

    @Override
    public ChangeSetFilterResult accepts(RawChangeSet changeSet) {
        String context = changeSet.getContexts();
        boolean strict = GlobalConfiguration.STRICT.getCurrentValue();

        if (strict && context != null && context.trim().length() == 0) {
            return new ChangeSetFilterResult(false, "context value cannot be empty while on Strict mode", ChangeSetFilter.class, "contextEmptyOnStrictMode", "context");
        }

        return new ChangeSetFilterResult(true, "Valid context value provided", ChangeSetFilter.class, "validContext", "context");
    }
}
