package liquibase.changelog;

import liquibase.ExecutionEnvironment;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.changelog.visitor.SkippedChangeSetVisitor;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

import java.util.*;

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

    public void run(ChangeSetVisitor visitor, ExecutionEnvironment env) throws LiquibaseException {
      Logger log = LogFactory.getLogger();
      log.setChangeLog(databaseChangeLog);
        try {
            List<ChangeSet> changeSetList = new ArrayList<ChangeSet>(databaseChangeLog.getChangeSets());
            if (visitor.getDirection().equals(ChangeSetVisitor.Direction.REVERSE)) {
                Collections.reverse(changeSetList);
            }

            for (ChangeSet changeSet : changeSetList) {
                boolean shouldVisit = true;
                Set<ChangeSetFilterResult> reasonsAccepted = new HashSet<ChangeSetFilterResult>();
                Set<ChangeSetFilterResult> reasonsDenied = new HashSet<ChangeSetFilterResult>();
                if (changeSetFilters != null) {
                    for (ChangeSetFilter filter : changeSetFilters) {
                        ChangeSetFilterResult acceptsResult = filter.accepts(changeSet);
                        if (acceptsResult.isAccepted()) {
                            reasonsAccepted.add(acceptsResult);
                        } else {
                            shouldVisit = false;
                            reasonsDenied.add(acceptsResult);
                            break;
                        }
                    }
                }

                log.setChangeSet(changeSet);
                if (shouldVisit) {
                    visitor.visit(changeSet, databaseChangeLog, env, reasonsAccepted);
                } else {
                    if (visitor instanceof SkippedChangeSetVisitor) {
                        ((SkippedChangeSetVisitor) visitor).skipped(changeSet, databaseChangeLog, env.getTargetDatabase(), reasonsDenied);
                    }
                }
                log.setChangeSet(null);
            }
        } finally {
            log.setChangeLog(null);
        }
    }

    public List<ChangeSetFilter> getChangeSetFilters() {
        return Collections.unmodifiableList(changeSetFilters);
    }
}
