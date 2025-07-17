package liquibase.validator.propertyvalidator;

import liquibase.GlobalConfiguration;
import liquibase.validator.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;

public class LogicalFilePathValidatorFilter implements ValidatorFilter {

    @Override
    public ChangeSetFilterResult accepts(RawChangeSet changeSet) {
        String logicalFilePath = changeSet.getLogicalFilePath();

        boolean strictValue = GlobalConfiguration.STRICT.getCurrentValue();
        if((strictValue && logicalFilePath != null && logicalFilePath.trim().length() == 0)) {
            return new ChangeSetFilterResult(false, "logicalFilePath value cannot be empty while on Strict mode", ChangeSetFilter.class, "logicalFilePathOnStrictMode", "logicalFilePath");
        }
        return new ChangeSetFilterResult(true, "logicalFilePath correctly set", ChangeSetFilter.class, "validLogicalFilePath", "logicalFilePath");

    }
}
