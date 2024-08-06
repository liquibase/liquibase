package liquibase.logging.mdc.customobjects;

import liquibase.changelog.ChangeSet;
import liquibase.logging.mdc.CustomMdcObject;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class Status extends SimpleStatus implements CustomMdcObject {

    private List<Changeset> undeployedChangesets;

    public Status() {
    }

    public Status(String message, String url, List<ChangeSet> unrunChangeSets) {
        super(message, url, unrunChangeSets);
        this.undeployedChangesets = unrunChangeSets.stream().map(urcs -> new Changeset(urcs.getFilePath(), urcs.getAuthor(), urcs.getId())).collect(Collectors.toList());
    }

    public void setUndeployedChangesets(List<Changeset> undeployedChangesets) {
        this.undeployedChangesets = undeployedChangesets;
    }

    @Getter
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
