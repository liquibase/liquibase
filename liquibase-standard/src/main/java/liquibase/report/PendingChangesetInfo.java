package liquibase.report;

import liquibase.changelog.ChangeSet;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PendingChangesetInfo {
    private String changesetAuthor;
    private String changesetId;
    private String changelogFile;
    private String comment;
    private String labels;
    private String contexts;
    private String reason;
    private ChangeSet changeSet;
}
