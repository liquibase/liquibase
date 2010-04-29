package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.statement.SqlStatement;

public class UpdateChangeSetChecksumStatement implements SqlStatement {

    private ChangeSet changeSet;

    public UpdateChangeSetChecksumStatement(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }
}