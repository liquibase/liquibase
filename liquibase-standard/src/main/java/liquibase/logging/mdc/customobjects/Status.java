package liquibase.logging.mdc.customobjects;

import liquibase.changelog.ChangeSet;
import liquibase.logging.mdc.CustomMdcObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class Status extends SimpleStatus implements CustomMdcObject {

    private List<Changeset> undeployedChangesets;

    public Status() {
    }

    public Status(String message, String url, List<ChangeSet> unrunChangeSets) {
        super(message, url, unrunChangeSets);
        this.undeployedChangesets = unrunChangeSets.stream().map(urcs -> new Changeset(urcs.getFilePath(), urcs.getAuthor(), urcs.getId())).collect(Collectors.toList());
    }

    @Setter
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

    }
}
