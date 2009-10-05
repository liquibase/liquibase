package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.SqlStatement;

public class MarkChangeSetRanStatement implements SqlStatement {

    private ChangeSet changeSet;

    private ChangeSet.ExecType execType;

    public MarkChangeSetRanStatement(ChangeSet changeSet, ChangeSet.ExecType execType) {
        this.changeSet = changeSet;
        this.execType = execType;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public ChangeSet.ExecType getExecType() {
        return execType;
    }
}
