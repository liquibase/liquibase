package liquibase.parser.filter;

import liquibase.ChangeSet;

public interface ChangeSetFilter {

    public boolean accepts(ChangeSet changeSet);
}
