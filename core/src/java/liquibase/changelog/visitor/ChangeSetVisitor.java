package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

public interface ChangeSetVisitor {

    public enum Direction {
        FORWARD,
        REVERSE
    };

    Direction getDirection(); 

    void visit(ChangeSet changeSet, Database database) throws LiquibaseException;
}
