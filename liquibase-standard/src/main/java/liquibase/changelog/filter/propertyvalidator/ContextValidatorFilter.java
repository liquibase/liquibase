package liquibase.changelog.filter.propertyvalidator;

import liquibase.GlobalConfiguration;
import liquibase.changelog.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;

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
