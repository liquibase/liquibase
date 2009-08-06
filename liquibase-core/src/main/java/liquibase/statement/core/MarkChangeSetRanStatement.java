package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.SqlStatement;

public class MarkChangeSetRanStatement implements SqlStatement {
    private ChangeSet changeSet;

    private boolean ranBefore = false;

    public MarkChangeSetRanStatement(ChangeSet changeSet, boolean ranBefore) {
        this.changeSet = changeSet;
        this.ranBefore = ranBefore;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public boolean isRanBefore() {
        return ranBefore;
    }
}
