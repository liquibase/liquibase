package liquibase.changelog;

import liquibase.LoggerContexts;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.SkippedChangeSetVisitor;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.exception.LiquibaseException;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                if (changeLog.ignoreClasspathPrefix()) {
                    changeSet.setFilePath(ranChangeSet.getChangeLog());
                }
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

    public void run(ChangeSetVisitor visitor, RuntimeEnvironment env) throws LiquibaseException {
      Logger log = LoggerFactory.getLogger(getClass());
      databaseChangeLog.setRuntimeEnvironment(env);
        MDC.put(LoggerContexts.CHANGELOG.name(), databaseChangeLog);
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

                MDC.put(LoggerContexts.CHANGESET.name(), changeSet);
                if (shouldVisit) {
                    visitor.visit(changeSet, databaseChangeLog, env.getTargetDatabase(), reasonsAccepted);
                } else {
                    if (visitor instanceof SkippedChangeSetVisitor) {
                        ((SkippedChangeSetVisitor) visitor).skipped(changeSet, databaseChangeLog, env.getTargetDatabase(), reasonsDenied);
                    }
                }
                MDC.remove(LoggerContexts.CHANGESET.name());
            }
        } finally {
            MDC.remove(LoggerContexts.CHANGELOG.name());
            databaseChangeLog.setRuntimeEnvironment(null);
        }
    }

    public List<ChangeSetFilter> getChangeSetFilters() {
        return Collections.unmodifiableList(changeSetFilters);
    }
}
