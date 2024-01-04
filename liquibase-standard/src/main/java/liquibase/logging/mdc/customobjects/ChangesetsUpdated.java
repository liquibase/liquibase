package liquibase.logging.mdc.customobjects;

import liquibase.changelog.ChangeSet;
import liquibase.logging.mdc.CustomMdcObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom MDC object to represent the changesets that were deployed during an update event.
 */
public class ChangesetsUpdated implements CustomMdcObject {

    private int changesetCount;
    private List<MdcChangesetExtended> changeset;

    /**
     * Constructor for service locator.
     */
    public ChangesetsUpdated() {
    }

    public ChangesetsUpdated(List<ChangeSet> deployedChangeSets) {
        this.changesetCount = deployedChangeSets.size();
        this.changeset = new ArrayList<>(this.changesetCount);
        for (ChangeSet deployedChangeSet : deployedChangeSets) {
            this.changeset.add(MdcChangesetExtended.fromChangeset(deployedChangeSet));
        }
    }

    public int getChangesetCount() {
        return changesetCount;
    }

    public void setChangesetCount(int changesetCount) {
        this.changesetCount = changesetCount;
    }

    public List<MdcChangesetExtended> getChangeset() {
        return changeset;
    }

    public void setChangeset(List<MdcChangesetExtended> changeset) {
        this.changeset = changeset;
    }

}
