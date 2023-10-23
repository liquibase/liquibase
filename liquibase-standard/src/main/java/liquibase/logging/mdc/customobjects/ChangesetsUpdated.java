package liquibase.logging.mdc.customobjects;

import liquibase.changelog.ChangeSet;
import liquibase.logging.mdc.CustomMdcObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Custom MDC object to represent the changesets that were deployed during an update event.
 */
public class ChangesetsUpdated implements CustomMdcObject {

    private int changesetCount;
    private List<Changeset> changeset;

    /**
     * Constructor for service locator.
     */
    public ChangesetsUpdated() {
    }

    public ChangesetsUpdated(List<ChangeSet> deployedChangeSets) {
        this.changesetCount = deployedChangeSets.size();
        this.changeset = new ArrayList<>(this.changesetCount);
        for (ChangeSet deployedChangeSet : deployedChangeSets) {
            this.changeset.add(new Changeset(
                    deployedChangeSet.getId(),
                    deployedChangeSet.getAuthor(),
                    deployedChangeSet.getFilePath(),
                    Objects.toString(deployedChangeSet.getAttribute("deploymentId")),
                    Objects.toString(deployedChangeSet.getAttribute("updateExecType"))));
        }
    }

    public int getChangesetCount() {
        return changesetCount;
    }

    public void setChangesetCount(int changesetCount) {
        this.changesetCount = changesetCount;
    }

    public List<Changeset> getChangeset() {
        return changeset;
    }

    public void setChangeset(List<Changeset> changeset) {
        this.changeset = changeset;
    }

    public static class Changeset {
        private String changesetId;
        private String changesetAuthor;
        private String changesetFilepath;
        private String deploymentId;
        private String changesetOutcome;

        public Changeset(String changesetId, String changesetAuthor, String changesetFilepath, String deploymentId, String changesetOutcome) {
            this.changesetId = changesetId;
            this.changesetAuthor = changesetAuthor;
            this.changesetFilepath = changesetFilepath;
            this.deploymentId = deploymentId;
            this.changesetOutcome = changesetOutcome;
        }

        public String getChangesetId() {
            return changesetId;
        }

        public void setChangesetId(String changesetId) {
            this.changesetId = changesetId;
        }

        public String getChangesetAuthor() {
            return changesetAuthor;
        }

        public void setChangesetAuthor(String changesetAuthor) {
            this.changesetAuthor = changesetAuthor;
        }

        public String getChangesetFilepath() {
            return changesetFilepath;
        }

        public void setChangesetFilepath(String changesetFilepath) {
            this.changesetFilepath = changesetFilepath;
        }

        public String getDeploymentId() {
            return deploymentId;
        }

        public void setDeploymentId(String deploymentId) {
            this.deploymentId = deploymentId;
        }

        public String getChangesetOutcome() {
            return changesetOutcome;
        }

        public void setChangesetOutcome(String changesetOutcome) {
            this.changesetOutcome = changesetOutcome;
        }
    }

}
