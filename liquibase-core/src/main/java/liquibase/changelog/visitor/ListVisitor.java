package liquibase.changelog.visitor;

import liquibase.ExecutionEnvironment;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.exception.LiquibaseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListVisitor implements ChangeSetVisitor {

    private List<ChangeSet> seenChangeSets = new ArrayList<ChangeSet>();

    public List<ChangeSet> getSeenChangeSets() {
        return seenChangeSets;
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, ExecutionEnvironment env, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        seenChangeSets.add(changeSet);
    }
}