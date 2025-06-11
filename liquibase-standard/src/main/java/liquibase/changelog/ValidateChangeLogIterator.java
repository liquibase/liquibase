package liquibase.changelog;

import liquibase.RuntimeEnvironment;
import liquibase.Scope;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.filter.CountChangeSetFilter;
import liquibase.changelog.filter.UpToTagChangeSetFilter;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.changelog.visitor.SkippedChangeSetVisitor;
import liquibase.exception.LiquibaseException;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ValidateChangeLogIterator extends ChangeLogIterator {

    @Getter
    private final List<ChangeSetFilterResult> reasonsDenied = new ArrayList<>();

    public ValidateChangeLogIterator(DatabaseChangeLog databaseChangeLog, ChangeSetFilter... changeSetFilters) {
        super(databaseChangeLog, changeSetFilters);
    }

    @Override
    public void run(ChangeSetVisitor visitor, RuntimeEnvironment env) throws LiquibaseException {
        databaseChangeLog.setRuntimeEnvironment(env);
        try {
            Scope.child(Scope.Attr.databaseChangeLog, databaseChangeLog, () -> {
                List<ChangeSet> changeSetList = new ArrayList<>(databaseChangeLog.getChangeSets());

                for (ChangeSet changeSet : changeSetList) {
                    if (changeSetFilters != null) {
                        iterateFilters(changeSet, reasonsDenied);
                    }
                }
            });
        } catch (Exception e) {
            throw new LiquibaseException(e);
        } finally {
            databaseChangeLog.setRuntimeEnvironment(null);
        }
    }

    private void iterateFilters(ChangeSet changeSet, List<ChangeSetFilterResult> reasonsDenied) {
        for (ChangeSetFilter filter : changeSetFilters) {
            ChangeSetFilterResult reason = filter.accepts(changeSet);
            if(!reason.isAccepted()){
                reasonsDenied.add(reason);
            }
        }
    }

}
