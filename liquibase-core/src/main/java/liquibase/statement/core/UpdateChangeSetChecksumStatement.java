package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

/**
 * Update the liquibase checksum for the given changeSet.
 */
public class UpdateChangeSetChecksumStatement extends AbstractStatement {

    public static final String CHANGE_SET = "changeSet";

    public UpdateChangeSetChecksumStatement() {
    }

    public UpdateChangeSetChecksumStatement(ChangeSet changeSet) {
        setAttribute(CHANGE_SET, changeSet);
    }

    public ChangeSet getChangeSet() {
        return getAttribute(CHANGE_SET, ChangeSet.class);
    }

    public UpdateChangeSetChecksumStatement setChangeSet(ChangeSet changeSet) {
        return (UpdateChangeSetChecksumStatement) setAttribute(CHANGE_SET, changeSet);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}