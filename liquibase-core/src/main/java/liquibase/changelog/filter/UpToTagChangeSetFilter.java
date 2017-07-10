package liquibase.changelog.filter;

import liquibase.change.Change;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

import java.util.List;

public class UpToTagChangeSetFilter implements ChangeSetFilter {
    private final String tag;
    private boolean seenTag;

    public UpToTagChangeSetFilter(String tag, List<RanChangeSet> ranChangeSets) {
        this.tag = tag;
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (tag.equalsIgnoreCase(ranChangeSet.getTag())) {
                seenTag = true;
                break;
            }
        }
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (seenTag) {
            return new ChangeSetFilterResult(false, "Change set is after tag '" + this.tag + "'", this.getClass());
        }

        String tag = null;
        for (Change change : changeSet.getChanges()) {
            if (change instanceof TagDatabaseChange) {
                tag = ((TagDatabaseChange) change).getTag();
            }
        }

        if (this.tag.equals(tag)) {
            seenTag = true;
        }

        return new ChangeSetFilterResult(true, "Change set is at or before tag '" + this.tag + "'", this.getClass());
    }
}
