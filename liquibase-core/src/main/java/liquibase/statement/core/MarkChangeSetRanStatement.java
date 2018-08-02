package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.definition.ChangeLogColumnDefinition;
import liquibase.changelog.value.*;
import liquibase.statement.AbstractSqlStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static liquibase.changelog.definition.ChangeLogTableDefinition.*;

public class MarkChangeSetRanStatement extends AbstractSqlStatement {

    private final ChangeSet changeSet;

    private final ChangeSet.ExecType execType;

    private final List<String> columnsForUpdate;

    private final List<String> columnsForInsert;

    private final Map<String, ChangeLogColumnValueProvider> columnValuesProviders;

    public MarkChangeSetRanStatement(ChangeSet changeSet, ChangeSet.ExecType execType) {
        this.changeSet = changeSet;
        this.execType = execType;
        this.columnsForInsert = createColumnsForInsert();
        this.columnsForUpdate = createColumnsForUpdate();
        this.columnValuesProviders = createColumnValueProviders();
    }

    public final void addColumnForUpdate(ChangeLogColumnDefinition columnDefinition, ChangeLogColumnValueProvider columnValueProvider) {
        this.columnsForUpdate.add(columnDefinition.getColumnName());
        addColumnValueProviderIfMissing(columnDefinition, columnValueProvider);
    }

    public final void addColumnForInsert(ChangeLogColumnDefinition columnDefinition, ChangeLogColumnValueProvider columnValueProvider) {
        this.columnsForInsert.add(columnDefinition.getColumnName());
        addColumnValueProviderIfMissing(columnDefinition, columnValueProvider);
    }

    private void addColumnValueProviderIfMissing(ChangeLogColumnDefinition columnDefinition, ChangeLogColumnValueProvider columnValueProvider) {
        String columnName = columnDefinition.getColumnName();
        if (!this.columnValuesProviders.containsKey(columnName)) {
            this.columnValuesProviders.put(columnName, columnValueProvider);
        }
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
        return this.columnValuesProviders;
    }

    private static List<String> createColumnsForInsert() {
        List<String> columnsForInsert = new ArrayList<>();

        columnsForInsert.add(ID);
        columnsForInsert.add(AUTHOR);
        columnsForInsert.add(FILENAME);
        columnsForInsert.add(DATEEXECUTED);
        columnsForInsert.add(ORDEREXECUTED);
        columnsForInsert.add(MD_5_SUM);
        columnsForInsert.add(DESCRIPTION);
        columnsForInsert.add(COMMENTS);
        columnsForInsert.add(EXECTYPE);
        columnsForInsert.add(CONTEXTS);
        columnsForInsert.add(LABELS);
        columnsForInsert.add(LIQUIBASE);
        columnsForInsert.add(DEPLOYMENT_ID);

        return columnsForInsert;
    }

    private static List<String> createColumnsForUpdate() {
        List<String> columnsForUpdate = new ArrayList<>();

        columnsForUpdate.add(DATEEXECUTED);
        columnsForUpdate.add(ORDEREXECUTED);
        columnsForUpdate.add(MD_5_SUM);
        columnsForUpdate.add(EXECTYPE);
        columnsForUpdate.add(DEPLOYMENT_ID);

        return columnsForUpdate;
    }

    private static Map<String, ChangeLogColumnValueProvider> createColumnValueProviders() {
        HashMap<String, ChangeLogColumnValueProvider> columnValueProviders = new HashMap<>();

        columnValueProviders.put(ID, new IdProvider());
        columnValueProviders.put(AUTHOR, new AuthorProvider());
        columnValueProviders.put(FILENAME, new FileNameProvider());
        columnValueProviders.put(DATEEXECUTED, new DateExecutedProvider());
        columnValueProviders.put(ORDEREXECUTED, new OrderExecutedProvider());
        columnValueProviders.put(MD_5_SUM, new MD5SUMProvider());
        columnValueProviders.put(DESCRIPTION, new DescriptionProvider());
        columnValueProviders.put(COMMENTS, new CommentsProvider());
        columnValueProviders.put(EXECTYPE, new ExecTypeProvider());
        columnValueProviders.put(CONTEXTS, new ContextsProvider());
        columnValueProviders.put(LABELS, new LabelsProvider());
        columnValueProviders.put(LIQUIBASE, new LiquibaseVersionProvider());
        columnValueProviders.put(DEPLOYMENT_ID, new DeploymentIdProvider());

        return columnValueProviders;
    }
}
