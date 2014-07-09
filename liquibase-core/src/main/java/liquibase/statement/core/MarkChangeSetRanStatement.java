package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

public class MarkChangeSetRanStatement extends AbstractStatement {

    public static final String CHANGE_SET = "changeSet";
    public static final String EXEC_TYPE = "execType";

    public MarkChangeSetRanStatement() {
    }

    public MarkChangeSetRanStatement(ChangeSet changeSet, ChangeSet.ExecType execType) {
        setChangeSet(changeSet);
        setExecType(execType);
    }

    public ChangeSet getChangeSet() {
        return getAttribute(CHANGE_SET, ChangeSet.class);
    }

    public MarkChangeSetRanStatement setChangeSet(ChangeSet changeSet) {
        return (MarkChangeSetRanStatement) setAttribute(CHANGE_SET, changeSet);
    }

    public ChangeSet.ExecType getExecType() {
        return getAttribute(EXEC_TYPE, ChangeSet.ExecType.class);
    }

    public MarkChangeSetRanStatement setExecType(ChangeSet.ExecType execType) {
        return (MarkChangeSetRanStatement) setAttribute(EXEC_TYPE, execType);
    }


    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
