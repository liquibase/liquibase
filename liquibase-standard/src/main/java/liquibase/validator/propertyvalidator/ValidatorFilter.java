package liquibase.validator.propertyvalidator;

import liquibase.validator.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilterResult;

public interface ValidatorFilter {

    ChangeSetFilterResult accepts(RawChangeSet changeSet);

}
