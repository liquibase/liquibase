package liquibase.validator.propertyvalidator;

import liquibase.validator.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilterResult;

/**
 * Interface for filters that validate properties of a {@link RawChangeSet}.
 */
public interface ValidatorFilter {

    ChangeSetFilterResult accepts(RawChangeSet changeSet);

}
