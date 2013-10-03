package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;

public interface ChangeSetFilter {

    public boolean accepts(ChangeSet changeSet);
}
