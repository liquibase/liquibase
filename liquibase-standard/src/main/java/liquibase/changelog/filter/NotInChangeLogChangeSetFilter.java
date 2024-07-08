package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

import java.util.HashSet;
import java.util.Set;

public class NotInChangeLogChangeSetFilter implements ChangeSetFilter {

    private final Set<ChangeSet> changeSets;

    public NotInChangeLogChangeSetFilter(DatabaseChangeLog databaseChangeLog) {
        this.changeSets = new HashSet<>(databaseChangeLog.getChangeSets());
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (changeSets.contains(changeSet)) {
            return new ChangeSetFilterResult(false, "Changeset is in change log", this.getClass(), getMdcName(), getDisplayName());
        } else {
            return new ChangeSetFilterResult(true, "Changeset is not in change log", this.getClass(), getMdcName(), getDisplayName());
        }
    }
}
