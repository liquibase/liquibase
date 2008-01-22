package liquibase.parser.visitor;

import liquibase.ChangeSet;
import liquibase.exception.LiquibaseException;
import liquibase.log.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ListVisitor implements ChangeSetVisitor {

    private Logger log = LogFactory.getLogger();

    private List<ChangeSet> seenChangeSets = new ArrayList<ChangeSet>();

    public List<ChangeSet> getSeenChangeSets() {
        return seenChangeSets;
    }

    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    public void visit(ChangeSet changeSet) throws LiquibaseException {
        seenChangeSets.add(changeSet);
    }
}