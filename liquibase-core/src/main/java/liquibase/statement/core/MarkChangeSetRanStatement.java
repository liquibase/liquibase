package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.AbstractSqlStatement;

import java.util.ArrayList;
import java.util.List;

public class MarkChangeSetRanStatement extends AbstractSqlStatement {

    private ChangeSet changeSet;

    private ChangeSet.ExecType execType;

    private List<String> columnsForUpdate;
    private List<String> columnsForInsert;

    public MarkChangeSetRanStatement(ChangeSet changeSet, ChangeSet.ExecType execType) {
        this.changeSet = changeSet;
        this.execType = execType;

        columnsForUpdate = new ArrayList<>();
        columnsForUpdate.add("DATEEXECUTED");
        columnsForUpdate.add("ORDEREXECUTED");
        columnsForUpdate.add("MD5SUM");
        columnsForUpdate.add("EXECTYPE");
        columnsForUpdate.add("DEPLOYMENT_ID");

        columnsForInsert = new ArrayList<>();
        columnsForInsert.add("ID");
        columnsForInsert.add("AUTHOR");
        columnsForInsert.add("FILENAME");
        columnsForInsert.add("DATEEXECUTED");
        columnsForInsert.add("ORDEREXECUTED");
        columnsForInsert.add("MD5SUM");
        columnsForInsert.add("DESCRIPTION");
        columnsForInsert.add("COMMENTS");
        columnsForInsert.add("EXECTYPE");
        columnsForInsert.add("CONTEXTS");
        columnsForInsert.add("LABELS");
        columnsForInsert.add("LIQUIBASE");
        columnsForInsert.add("DEPLOYMENT_ID");
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public ChangeSet.ExecType getExecType() {
        return execType;
    }

    public List<String> getColumnsForUpdate() {
        return columnsForUpdate;
    }

    public List<String> getColumnsForInsert() {
        return columnsForInsert;
    }
}
