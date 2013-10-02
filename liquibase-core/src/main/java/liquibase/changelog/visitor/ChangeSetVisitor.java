package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

public interface ChangeSetVisitor {

    public enum Direction {
        FORWARD,
        REVERSE
    };

    Direction getDirection(); 

    void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database) throws LiquibaseException;
}
