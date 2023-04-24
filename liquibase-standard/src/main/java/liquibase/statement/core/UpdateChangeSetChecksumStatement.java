package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.AbstractSqlStatement;

public class UpdateChangeSetChecksumStatement extends AbstractSqlStatement {

    private ChangeSet changeSet;

    public UpdateChangeSetChecksumStatement(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }
}