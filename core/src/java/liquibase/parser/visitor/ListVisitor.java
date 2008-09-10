package liquibase.parser.visitor;

import liquibase.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

import java.util.ArrayList;
import java.util.List;

public class ListVisitor implements ChangeSetVisitor {

    private List<ChangeSet> seenChangeSets = new ArrayList<ChangeSet>();

    public List<ChangeSet> getSeenChangeSets() {
        return seenChangeSets;
    }

    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    public void visit(ChangeSet changeSet, Database database) throws LiquibaseException {
        seenChangeSets.add(changeSet);
    }
}