package liquibase.changelog.filter;

import liquibase.change.Change;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

import java.util.List;

public class UpToTagChangeSetFilter implements ChangeSetFilter {
    private final String tag;
    private boolean seenTag;
    private String ranChangesetTagId = null;


    public UpToTagChangeSetFilter(String tag, List<RanChangeSet> ranChangeSets) {
        this.tag = tag;
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (tag.equalsIgnoreCase(ranChangeSet.getTag())) {
                ranChangesetTagId = ranChangeSet.toString();
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

        // if the tag is already in the database, accept the changesets until we find it
        if (changeSet.toString().equals(this.ranChangesetTagId)) {
            seenTag = true;
        // otherwise validate each new changeset until we find the tag
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
