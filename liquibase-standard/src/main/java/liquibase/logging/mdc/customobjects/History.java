package liquibase.logging.mdc.customobjects;

import liquibase.changelog.RanChangeSet;
import liquibase.logging.mdc.CustomMdcObject;

import java.util.List;

public class History implements CustomMdcObject {

    private String liquibaseTargetUrl;
    private int changesetCount;
    private List<Changeset> changesets;

    public History() {
    }

    public History(String liquibaseTargetUrl, int changesetCount, List<Changeset> changesets) {
        this.liquibaseTargetUrl = liquibaseTargetUrl;
        this.changesetCount = changesetCount;
        this.changesets = changesets;
    }

    public String getLiquibaseTargetUrl() {
        return liquibaseTargetUrl;
    }

    public void setLiquibaseTargetUrl(String liquibaseTargetUrl) {
        this.liquibaseTargetUrl = liquibaseTargetUrl;
    }

    public int getChangesetCount() {
        return changesetCount;
    }

    public void setChangesetCount(int changesetCount) {
        this.changesetCount = changesetCount;
    }

    public List<Changeset> getChangesets() {
        return changesets;
    }

    public void setChangesets(List<Changeset> changesets) {
        this.changesets = changesets;
    }

    public static class Changeset {
        private String deploymentId;
        private String updateDate;
        private String changelogPath;
        private String changesetAuthor;
        private String changesetId;

        public Changeset() {
        }

        public Changeset(RanChangeSet ranChangeSet) {
            this.deploymentId = ranChangeSet.getDeploymentId();
            this.updateDate = ranChangeSet.getDateExecuted().toString();
            this.changelogPath = ranChangeSet.getChangeLog();
            this.changesetAuthor = ranChangeSet.getAuthor();
            this.changesetId = ranChangeSet.getId();
        }

        public String getDeploymentId() {
            return deploymentId;
        }

        public void setDeploymentId(String deploymentId) {
            this.deploymentId = deploymentId;
        }

        public String getUpdateDate() {
            return updateDate;
        }

        public void setUpdateDate(String updateDate) {
            this.updateDate = updateDate;
        }

        public String getChangelogPath() {
            return changelogPath;
        }

        public void setChangelogPath(String changelogPath) {
            this.changelogPath = changelogPath;
        }

        public String getChangesetAuthor() {
            return changesetAuthor;
        }

        public void setChangesetAuthor(String changesetAuthor) {
            this.changesetAuthor = changesetAuthor;
        }

        public String getChangesetId() {
            return changesetId;
        }

        public void setChangesetId(String changesetId) {
            this.changesetId = changesetId;
        }
    }

}
