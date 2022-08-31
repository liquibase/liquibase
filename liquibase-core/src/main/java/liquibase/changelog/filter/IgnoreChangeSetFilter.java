package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;

public class IgnoreChangeSetFilter implements ChangeSetFilter {
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (changeSet.isIgnore()) {
            return new ChangeSetFilterResult(false, "Changeset is ignored", this.getClass());
        }
        else {
            if (changeSet.isInheritableIgnore()) {
                return new ChangeSetFilterResult(false, "Changeset is ignored", this.getClass());
            }
        }
        return new ChangeSetFilterResult(true, "Changeset is not ignored", this.getClass());
    }
}
