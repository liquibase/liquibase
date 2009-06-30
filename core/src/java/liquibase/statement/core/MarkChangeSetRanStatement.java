package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.SqlStatement;

public class MarkChangeSetRanStatement implements SqlStatement {
    private ChangeSet changeSet;

    public MarkChangeSetRanStatement(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }
}
