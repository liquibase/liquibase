package liquibase.changelog.visitor;

import liquibase.ExecutionEnvironment;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.exception.LiquibaseException;

import java.util.Set;

/**
 * Called by {@link liquibase.changelog.ChangeLogIterator} when a {@link liquibase.changelog.filter.ChangeSetFilter} accept a changeSet.
 *
 * @see liquibase.changelog.visitor.SkippedChangeSetVisitor
 *
 */
public interface ChangeSetVisitor {

    public enum Direction {
        FORWARD,
        REVERSE
    }

    Direction getDirection(); 

    void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, ExecutionEnvironment env, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException;
}
