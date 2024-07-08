package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

import java.util.Set;

/**
 * Called by {@link liquibase.changelog.ChangeLogIterator} when a {@link liquibase.changelog.filter.ChangeSetFilter} rejects a changeSet.
 * To use, {@link liquibase.changelog.visitor.ChangeSetVisitor} implementations should implement this interface as well.
 */
public interface SkippedChangeSetVisitor {

    void skipped(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException;

}
