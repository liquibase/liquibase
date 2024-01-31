package liquibase.logging.mdc.customobjects;

import liquibase.changelog.ChangeSet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class MdcChangesetExtended extends MdcChangeset {
    private String deploymentId;
    private String changesetOutcome;

    public MdcChangesetExtended(String changesetId, String changesetAuthor, String changesetFilepath, String deploymentId, String changesetOutcome) {
        super(changesetId, changesetAuthor, changesetFilepath);
        this.deploymentId = deploymentId;
        this.changesetOutcome = changesetOutcome;
    }

    public static MdcChangesetExtended fromChangeset(ChangeSet changeSet) {
        return new MdcChangesetExtended(
                changeSet.getId(),
                changeSet.getAuthor(),
                changeSet.getFilePath(),
                Objects.toString(changeSet.getAttribute("deploymentId")),
                Objects.toString(changeSet.getAttribute("updateExecType")));
    }
}
