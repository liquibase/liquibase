package liquibase.changelog.filter.propertyvalidator;

import liquibase.changelog.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilterResult;

public interface ValidatorFilter {

    ChangeSetFilterResult accepts(RawChangeSet changeSet);

}
