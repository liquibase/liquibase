package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.SqlStatement;

public class RemoveChangeSetRanStatusStatement implements SqlStatement {
    private ChangeSet changeSet;

    public RemoveChangeSetRanStatusStatement(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }
}
