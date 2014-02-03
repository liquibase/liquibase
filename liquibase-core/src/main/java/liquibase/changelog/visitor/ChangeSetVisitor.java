package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

import java.util.Set;

public interface ChangeSetVisitor {

    public enum Direction {
        FORWARD,
        REVERSE
    }

    Direction getDirection(); 

    void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException;
}
