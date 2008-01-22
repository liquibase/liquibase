package liquibase.parser.visitor;

import liquibase.ChangeSet;
import liquibase.exception.LiquibaseException;

public interface ChangeSetVisitor {

    public enum Direction {
        FORWARD,
        REVERSE
    };

    Direction getDirection(); 

    void visit(ChangeSet changeSet) throws LiquibaseException;
}
