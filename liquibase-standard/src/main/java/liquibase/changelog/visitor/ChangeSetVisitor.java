package liquibase.changelog.visitor;

import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.logging.mdc.MdcKey;

import java.util.Set;

/**
 * Called by {@link liquibase.changelog.ChangeLogIterator} when a {@link liquibase.changelog.filter.ChangeSetFilter} accept a changeSet.
 *
 * @see liquibase.changelog.visitor.SkippedChangeSetVisitor
 *
 */
public interface ChangeSetVisitor {

    enum Direction {
        FORWARD,
        REVERSE
    }

    Direction getDirection(); 

    void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException;

    default void logMdcData(ChangeSet changeSet) {
        Scope scope = Scope.getCurrentScope();
        scope.addMdcValue(MdcKey.CHANGESET_ID, changeSet.getId(), false);
        scope.addMdcValue(MdcKey.CHANGESET_AUTHOR, changeSet.getAuthor(), false);
        scope.addMdcValue(MdcKey.CHANGESET_FILEPATH, changeSet.getFilePath());
    }
}
