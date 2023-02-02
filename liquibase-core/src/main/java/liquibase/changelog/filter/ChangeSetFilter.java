package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;

public interface ChangeSetFilter {

    ChangeSetFilterResult accepts(ChangeSet changeSet);
}
