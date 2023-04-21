package liquibase.logging.mdc.customobjects;

import liquibase.logging.mdc.CustomMdcObject;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChangesetsRolledback implements CustomMdcObject {

    private int changesetCount;
    private List<ChangeSet> changesets;

    /**
     * Constructor for service locator.
     */
    public ChangesetsRolledback() {
    }

    public ChangesetsRolledback(List<ChangeSet> changeSets) {
        changesetCount = changeSets.size();
        this.changesets = changeSets;
    }

    public int getChangesetCount() {
        return changesetCount;
    }

    public void setChangesetCount(int changesetCount) {
        this.changesetCount = changesetCount;
    }

    public List<ChangeSet> getChangesets() {
        return changesets;
    }

    public void setChangesets(List<ChangeSet> changesets) {
        this.changesets = changesets;
    }

    /**
     * Generate a {@link ChangesetsRolledback} object from a list of {@link liquibase.changelog.ChangeSet}s.
     */
    public static ChangesetsRolledback fromChangesetList(List<liquibase.changelog.ChangeSet> changeSets) {
        if (changeSets != null) {
            List<ChangeSet> changesets = changeSets.stream().map(ChangeSet::fromChangeSet).collect(Collectors.toList());
            return new ChangesetsRolledback(changesets);
        } else {
            return new ChangesetsRolledback(Collections.emptyList());
        }
    }

    public static class ChangeSet {

        private String changesetId;
        private String author;
        private String filepath;
        private String deploymentId;

        public ChangeSet(String changesetId, String author, String filepath, String deploymentId) {
            this.changesetId = changesetId;
            this.author = author;
            this.filepath = filepath;
            this.deploymentId = deploymentId;
        }

        public String getChangesetId() {
            return changesetId;
        }

        public void setChangesetId(String changesetId) {
            this.changesetId = changesetId;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getFilepath() {
            return filepath;
        }

        public void setFilepath(String filepath) {
            this.filepath = filepath;
        }

        public String getDeploymentId() {
            return deploymentId;
        }

        public void setDeploymentId(String deploymentId) {
            this.deploymentId = deploymentId;
        }

        public static ChangeSet fromChangeSet(liquibase.changelog.ChangeSet changeSet) {
            return new ChangeSet(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath(), changeSet.getDeploymentId());
        }
    }


}
