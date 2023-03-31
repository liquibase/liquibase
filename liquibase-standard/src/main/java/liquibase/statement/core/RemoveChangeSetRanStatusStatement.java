package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.AbstractSqlStatement;

public class RemoveChangeSetRanStatusStatement extends AbstractSqlStatement {
    private ChangeSet changeSet;

    public RemoveChangeSetRanStatusStatement(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }
}
