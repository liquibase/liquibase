package liquibase.changelog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

public class ChangeLogIterator {
    private DatabaseChangeLog databaseChangeLog;
    private List<ChangeSetFilter> changeSetFilters;

    public ChangeLogIterator(DatabaseChangeLog databaseChangeLog, ChangeSetFilter... changeSetFilters) {
        this.databaseChangeLog = databaseChangeLog;
        this.changeSetFilters = Arrays.asList(changeSetFilters);
    }

    public ChangeLogIterator(List<RanChangeSet> changeSetList, DatabaseChangeLog changeLog, ChangeSetFilter... changeSetFilters) {
        final List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
        for (RanChangeSet ranChangeSet : changeSetList) {
        	ChangeSet changeSet = changeLog.getChangeSet(ranChangeSet);
        	if (changeSet != null) {
        		changeSets.add(changeSet);
        	}
        }
        this.databaseChangeLog = (new DatabaseChangeLog() {
            @Override
            public List<ChangeSet> getChangeSets() {
                return changeSets;
            }
        });

        this.changeSetFilters = Arrays.asList(changeSetFilters);
    }

    public void run(ChangeSetVisitor visitor, Database database) throws LiquibaseException {
      Logger log = LogFactory.getLogger();
      log.setChangeLog(databaseChangeLog);
        try {
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
                    log.setChangeSet(changeSet);
                    visitor.visit(changeSet, databaseChangeLog, database);
                    log.setChangeSet(null);
                }
            }
        } finally {
            log.setChangeLog(null);
        }
    }

    public List<ChangeSetFilter> getChangeSetFilters() {
        return Collections.unmodifiableList(changeSetFilters);
    }
}
