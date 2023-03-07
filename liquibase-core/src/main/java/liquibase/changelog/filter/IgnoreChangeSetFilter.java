package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;

public class IgnoreChangeSetFilter implements ChangeSetFilter {
    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (changeSet.isIgnore()) {
            return new ChangeSetFilterResult(false, "Changeset is ignored", this.getClass(), getDisplayName());
        }
        else {
            if (changeSet.isInheritableIgnore()) {
                return new ChangeSetFilterResult(false, "Changeset is ignored", this.getClass(), getDisplayName());
            }
        }
        return new ChangeSetFilterResult(true, "Changeset is not ignored", this.getClass(), getDisplayName());
    }

    @Override
    public String getDisplayName() {
        return "ignored";
    }
}
