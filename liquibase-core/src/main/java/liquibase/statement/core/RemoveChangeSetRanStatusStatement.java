package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

public class RemoveChangeSetRanStatusStatement extends AbstractStatement {
    private ChangeSet changeSet;

    public RemoveChangeSetRanStatusStatement(ChangeSet changeSet) {
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
