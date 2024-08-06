package liquibase.logging.mdc.customobjects;

import liquibase.changelog.ChangeSet;
import liquibase.logging.mdc.CustomMdcObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom MDC object to represent the changesets that were deployed during an update event.
 */
@Getter
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

    public void setChangesetCount(int changesetCount) {
        this.changesetCount = changesetCount;
    }

    public void setChangeset(List<MdcChangesetExtended> changeset) {
        this.changeset = changeset;
    }

}
