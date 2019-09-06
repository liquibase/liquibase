package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;

public class IgnoreChangeSetFilter implements ChangeSetFilter {
    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (changeSet.isIgnore()) {
            return new ChangeSetFilterResult(false, "Change set is ignored", this.getClass());
        }
        else {
            if (changeSet.isInheritableIgnore()) {
                return new ChangeSetFilterResult(false, "Change set is ignored", this.getClass());
            }
        }
        return new ChangeSetFilterResult(true, "Change set is not ignored", this.getClass());
    }
}
