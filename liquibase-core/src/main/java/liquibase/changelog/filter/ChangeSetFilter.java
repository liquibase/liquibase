package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;

public interface ChangeSetFilter {

    public ChangeSetFilterResult accepts(ChangeSet changeSet);
}
