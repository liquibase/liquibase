package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;

public class IgnoreChangeSetFilter implements ChangeSetFilter {
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (changeSet.isIgnore()) {
            return new ChangeSetFilterResult(false, "Change set is ignored", this.getClass());
        }
        return new ChangeSetFilterResult(true, "Change set is not ignored", this.getClass());
    }
}
