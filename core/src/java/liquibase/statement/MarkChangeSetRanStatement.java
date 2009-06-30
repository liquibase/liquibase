package liquibase.statement;

import liquibase.changelog.ChangeSet;

public class MarkChangeSetRanStatement implements SqlStatement {
    private ChangeSet changeSet;

    public MarkChangeSetRanStatement(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }
}
