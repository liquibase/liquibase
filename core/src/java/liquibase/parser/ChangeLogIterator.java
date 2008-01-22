package liquibase.parser;

import liquibase.ChangeSet;
import liquibase.DatabaseChangeLog;
import liquibase.exception.LiquibaseException;
import liquibase.parser.filter.ChangeSetFilter;
import liquibase.parser.visitor.ChangeSetVisitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChangeLogIterator {
    private DatabaseChangeLog databaseChangeLog;
    private List<ChangeSetFilter> changeSetFilters;

    public ChangeLogIterator(DatabaseChangeLog databaseChangeLog, ChangeSetFilter... changeSetFilters) {
        this.databaseChangeLog = databaseChangeLog;
        this.changeSetFilters = Arrays.asList(changeSetFilters);
    }

    public void run(ChangeSetVisitor visitor) throws LiquibaseException {
        List<ChangeSet> changeSetList = databaseChangeLog.getChangeSets();
        if (visitor.getDirection().equals(ChangeSetVisitor.Direction.REVERSE)) {
            Collections.reverse(changeSetList);
        }

        for (ChangeSet changeSet : changeSetList) {
            boolean shouldVisit = true;
            if (changeSetFilters != null) {
                for (ChangeSetFilter filter : changeSetFilters) {
                    if (!filter.accepts(changeSet)) {
                        shouldVisit = false;
                        break;
                    }
                }
            }

            if (shouldVisit) {
                visitor.visit(changeSet);
            }
        }
    }
}
