package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

public class UpdateChangeSetChecksumStatement extends AbstractStatement {

    private ChangeSet changeSet;

    public UpdateChangeSetChecksumStatement(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}