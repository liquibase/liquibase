package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.values.ChangeLogColumnValueProvider;
import liquibase.statement.AbstractSqlStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarkChangeSetRanStatement extends AbstractSqlStatement {

    private ChangeSet changeSet;

    private ChangeSet.ExecType execType;

    private List<String> columnsForUpdate;
    private List<String> columnsForInsert;

    public MarkChangeSetRanStatement(ChangeSet changeSet, ChangeSet.ExecType execType) {
        this.changeSet = changeSet;
        this.execType = execType;

        columnsForUpdate = new ArrayList<>();
        columnsForInsert = new ArrayList<>();

        addColumnForUpdate("DATEEXECUTED");
        addColumnForUpdate("ORDEREXECUTED");
        addColumnForUpdate("MD5SUM");
        addColumnForUpdate("EXECTYPE");
        addColumnForUpdate("DEPLOYMENT_ID");

        addColumnForInsert("ID");
        addColumnForInsert("AUTHOR");
        addColumnForInsert("FILENAME");
        addColumnForInsert("DATEEXECUTED");
        addColumnForInsert("ORDEREXECUTED");
        addColumnForInsert("MD5SUM");
        addColumnForInsert("DESCRIPTION");
        addColumnForInsert("COMMENTS");
        addColumnForInsert("EXECTYPE");
        addColumnForInsert("CONTEXTS");
        addColumnForInsert("LABELS");
        addColumnForInsert("LIQUIBASE");
        addColumnForInsert("DEPLOYMENT_ID");
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

    public Map<String, ChangeLogColumnValueProvider> getColumnValuesProviders() {
        HashMap<String, ChangeLogColumnValueProvider> columnValueProviders = new HashMap<>();

        columnValueProviders.put("ID", new ChangeLogColumnValueProvider.IdProvider());
        columnValueProviders.put("AUTHOR", new ChangeLogColumnValueProvider.AuthorProvider());
        columnValueProviders.put("FILENAME", new ChangeLogColumnValueProvider.FileNameProvider());
        columnValueProviders.put("DATEEXECUTED", new ChangeLogColumnValueProvider.DateExecutedProvider());
        columnValueProviders.put("ORDEREXECUTED", new ChangeLogColumnValueProvider.OrderExecutedProvider());
        columnValueProviders.put("MD5SUM", new ChangeLogColumnValueProvider.MD5SUMProvider());
        columnValueProviders.put("DESCRIPTION", new ChangeLogColumnValueProvider.DescriptionProvider());
        columnValueProviders.put("COMMENTS", new ChangeLogColumnValueProvider.CommentsProvider());
        columnValueProviders.put("EXECTYPE", new ChangeLogColumnValueProvider.ExecTypeProvider());
        columnValueProviders.put("CONTEXTS", new ChangeLogColumnValueProvider.ContextsProvider());
        columnValueProviders.put("LABELS", new ChangeLogColumnValueProvider.LabelsProvider());
        columnValueProviders.put("LIQUIBASE", new ChangeLogColumnValueProvider.LiquibaseVersionProvider());
        columnValueProviders.put("DEPLOYMENT_ID", new ChangeLogColumnValueProvider.DeploymentIdProvider());

        return columnValueProviders;
    }

    public void addColumnForUpdate(String columnName){
        columnsForUpdate.add(columnName);
    }

    public void addColumnForInsert(String columnName){
        columnsForInsert.add(columnName);
    }
}
