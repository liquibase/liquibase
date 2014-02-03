package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

public interface SkippedChangeSetVisitor {

    void skipped(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, ChangeSetFilterResult filterResult) throws LiquibaseException;

}
