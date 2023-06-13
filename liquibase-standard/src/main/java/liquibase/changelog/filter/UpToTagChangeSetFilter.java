package liquibase.changelog.filter;

import liquibase.change.Change;
import liquibase.change.CheckSum;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

import java.util.List;

public class UpToTagChangeSetFilter implements ChangeSetFilter {
    private final String tag;
    private boolean seenTag;
    private CheckSum checksumForTagChangeset = null;


    public UpToTagChangeSetFilter(String tag, List<RanChangeSet> ranChangeSets) {
        this.tag = tag;
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (tag.equalsIgnoreCase(ranChangeSet.getTag())) {
                checksumForTagChangeset = ranChangeSet.getLastCheckSum();
                break;
            }
        }

    }

    public boolean isSeenTag() {
        return seenTag;
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (seenTag) {
            return new ChangeSetFilterResult(false, "Changeset is after tag '" + this.tag + "'", this.getClass(), getMdcName(), getDisplayName());
        }

        if (this.checksumForTagChangeset != null && changeSet.isCheckSumValid(this.checksumForTagChangeset)) {
            seenTag = true;
        } else {
            String changesetTag = null;
            for (Change change : changeSet.getChanges()) {
                if (change instanceof TagDatabaseChange) {
                    changesetTag = ((TagDatabaseChange) change).getTag();
                }
            }

            if (this.tag.equals(changesetTag)) {
                seenTag = true;
            }
        }

        return new ChangeSetFilterResult(true, "Changeset is at or before tag '" + this.tag + "'", this.getClass(), getMdcName(), getDisplayName());
    }

    @Override
    public String getMdcName() {
        return "afterTag";
    }

    @Override
    public String getDisplayName() {
        return "After tag";
    }
}
