package liquibase.logging.mdc.customobjects;

import liquibase.changelog.RanChangeSet;
import liquibase.logging.mdc.CustomMdcObject;
import lombok.Getter;

import java.util.List;

@Getter
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

    public void setLiquibaseTargetUrl(String liquibaseTargetUrl) {
        this.liquibaseTargetUrl = liquibaseTargetUrl;
    }

    public void setChangesetCount(int changesetCount) {
        this.changesetCount = changesetCount;
    }

    public void setChangesets(List<Changeset> changesets) {
        this.changesets = changesets;
    }

    @Getter
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

        public void setDeploymentId(String deploymentId) {
            this.deploymentId = deploymentId;
        }

        public void setUpdateDate(String updateDate) {
            this.updateDate = updateDate;
        }

        public void setChangelogPath(String changelogPath) {
            this.changelogPath = changelogPath;
        }

        public void setChangesetAuthor(String changesetAuthor) {
            this.changesetAuthor = changesetAuthor;
        }

        public void setChangesetId(String changesetId) {
            this.changesetId = changesetId;
        }
    }

}
