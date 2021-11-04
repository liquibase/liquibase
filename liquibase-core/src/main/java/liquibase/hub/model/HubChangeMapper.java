package liquibase.hub.model;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.util.ISODateFormat;

import java.text.ParseException;
import java.util.Date;

public class HubChangeMapper {

    public static HubChange mapToHubChange(RanChangeSet ranChangeSet) {
        HubChange hubChange = new HubChange();
        hubChange.setChangesetId(ranChangeSet.getId());
        hubChange.setChangesetAuthor(ranChangeSet.getAuthor());
        hubChange.setChangesetFilename(ranChangeSet.getChangeLog());
        hubChange.setDescription(ranChangeSet.getDescription());
        hubChange.setComments(ranChangeSet.getComments());
        hubChange.setTag(ranChangeSet.getTag());
        hubChange.setLiquibase(ranChangeSet.getLiquibaseVersion());
        hubChange.setOrderExecuted(ranChangeSet.getOrderExecuted());
        hubChange.setExecType(ranChangeSet.getExecType().value);
        hubChange.setDeploymentId(ranChangeSet.getDeploymentId());
        hubChange.setDateExecuted(ranChangeSet.getDateExecuted());
        if (ranChangeSet.getContextExpression() != null) {
            hubChange.setContexts(ranChangeSet.getContextExpression().toString());
        }
        if (ranChangeSet.getLabels() != null) {
            hubChange.setLabels(ranChangeSet.getLabels().toString());
        }
        if (ranChangeSet.getLastCheckSum() != null) {
            hubChange.setMd5sum(ranChangeSet.getLastCheckSum().toString());
        }

        return hubChange;
    }

    public static HubChange mapToHubChange(ChangeSet changeSet) {
        HubChange hubChange = new HubChange();
        hubChange.setChangesetId(changeSet.getId());
        hubChange.setChangesetAuthor(changeSet.getAuthor());
        hubChange.setChangesetFilename(changeSet.getFilePath());
        hubChange.setDescription(changeSet.getDescription());
        hubChange.setComments(changeSet.getComments());
        // Contexts can't be null because of ChangeSet constructor logic
        hubChange.setContexts(changeSet.getContexts().toString());
        hubChange.setOrderExecuted(0);
        // CheckSum can't be null because of ChangeSet generateCheckSum logic
        hubChange.setMd5sum(changeSet.generateCheckSum().toString());
        hubChange.setExecType("EXECUTED");

        if (changeSet.getLabels() != null) {
            hubChange.setLabels(changeSet.getLabels().toString());
        }

        ISODateFormat iso = new ISODateFormat();
        try {
            hubChange.setDateExecuted(iso.parse(new Date().toString()));
        } catch (ParseException pe) {
            hubChange.setDateExecuted(new Date());
        }

        return hubChange;
    }
}
