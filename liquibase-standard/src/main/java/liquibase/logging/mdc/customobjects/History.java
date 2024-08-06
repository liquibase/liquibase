package liquibase.logging.mdc.customobjects;

import liquibase.changelog.RanChangeSet;
import liquibase.logging.mdc.CustomMdcObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
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

    @Setter
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

    }

}
