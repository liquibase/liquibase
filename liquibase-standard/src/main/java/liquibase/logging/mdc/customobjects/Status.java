package liquibase.logging.mdc.customobjects;

import liquibase.changelog.ChangeSet;
import liquibase.logging.mdc.CustomMdcObject;

import java.util.List;
import java.util.stream.Collectors;

public class Status extends SimpleStatus implements CustomMdcObject {

    private List<Changeset> undeployedChangesets;

    public Status() {
    }

    public Status(String message, String url, List<ChangeSet> unrunChangeSets) {
        super(message, url, unrunChangeSets);
        this.undeployedChangesets = unrunChangeSets.stream().map(urcs -> new Changeset(urcs.getFilePath(), urcs.getAuthor(), urcs.getId())).collect(Collectors.toList());
    }

    public List<Changeset> getUndeployedChangesets() {
        return undeployedChangesets;
    }

    public void setUndeployedChangesets(List<Changeset> undeployedChangesets) {
        this.undeployedChangesets = undeployedChangesets;
    }

    public static class Changeset {
        private String changelogPath;
        private String changesetAuthor;
        private String changesetId;

        public Changeset() {
        }

        public Changeset(String changelogPath, String changesetAuthor, String changesetId) {
            this.changelogPath = changelogPath;
            this.changesetAuthor = changesetAuthor;
            this.changesetId = changesetId;
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
