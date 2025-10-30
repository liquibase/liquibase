package liquibase.validator.propertyvalidator;

import liquibase.GlobalConfiguration;
import liquibase.validator.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;

/**
 * Validates the labels property of a {@link RawChangeSet}. Ensures that the labels is not empty when strict mode is enabled.
 * If the labels is empty in strict mode, it won't accept the provided changeSet and will provide a validation error message.
 */
public class LabelsValidatorFilter implements ValidatorFilter {

        @Override
        public ChangeSetFilterResult accepts(RawChangeSet changeSet) {
            String allLabels = changeSet.getLabels();

            boolean strictValue = GlobalConfiguration.STRICT.getCurrentValue();
            if(strictValue && allLabels!= null && allLabels.trim().length() == 0) {
                return new ChangeSetFilterResult(false, "labels value cannot be empty while on Strict mode", ChangeSetFilter.class, "labelsEmptyOnStrictMode", "labels");
            }
            return new ChangeSetFilterResult(true, "valid labels value provided", ChangeSetFilter.class, "validLabels", "labels");
        }
}
