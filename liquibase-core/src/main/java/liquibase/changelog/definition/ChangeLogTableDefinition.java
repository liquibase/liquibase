package liquibase.changelog.definition;

import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.IntType;
import liquibase.datatype.core.VarcharType;
import liquibase.statement.NotNullConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeLogTableDefinition {

    private static final int DESCRIPTON_SIZE = 255;
    private static final int TAG_SIZE = 255;
    private static final int COMMENTS_SIZE = 255;
    private static final int LIQUIBASE_SIZE = 20;
    private static final int MD5SUM_SIZE = 35;
    private static final int EXECTYPE_SIZE = 10;
    private static final int ORDEREXECUTED_DEFAULT_VALUE = -1;
    private static final int DEPLOYMENT_COLUMN_SIZE = 10;
    private static final int LABELS_SIZE = 255;
    private static final int CONTEXTS_SIZE = 255;

    private static final String ORDEREXECUTED = "ORDEREXECUTED";
    private static final String DEPLOYMENT_ID = "DEPLOYMENT_ID";
    private static final String LIQUIBASE = "LIQUIBASE";
    private static final String MD_5_SUM = "MD5SUM";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String TAG = "TAG";
    private static final String COMMENTS = "COMMENTS";
    private static final String EXECTYPE = "EXECTYPE";
    private static final String LABELS = "LABELS";
    private static final String CONTEXTS = "CONTEXTS";

    private List<AlterChangeLogTableSqlStatementProvider> updateTableSqlStatementProviders = new ArrayList<>();
    private Map<String, ChangeLogColumnDefinition> columnDefinitions = new HashMap<>();

    public ChangeLogTableDefinition() {
        columnDefinitions.put(ORDEREXECUTED, new ChangeLogColumnDefinition(ORDEREXECUTED, getInt(), ORDEREXECUTED_DEFAULT_VALUE, new NotNullConstraint()));
        columnDefinitions.put(DEPLOYMENT_ID, new ChangeLogColumnDefinition(DEPLOYMENT_ID, getVarchar(DEPLOYMENT_COLUMN_SIZE)));
        columnDefinitions.put(LIQUIBASE, new ChangeLogColumnDefinition(LIQUIBASE, getVarchar(LIQUIBASE_SIZE)));
        columnDefinitions.put(MD_5_SUM, new ChangeLogColumnDefinition(MD_5_SUM, getVarchar(MD5SUM_SIZE)));
        columnDefinitions.put(DESCRIPTION, new ChangeLogColumnDefinition(DESCRIPTION, getVarchar(DESCRIPTON_SIZE)));
        columnDefinitions.put(TAG, new ChangeLogColumnDefinition(TAG, getVarchar(TAG_SIZE)));
        columnDefinitions.put(COMMENTS, new ChangeLogColumnDefinition(COMMENTS, getVarchar(COMMENTS_SIZE)));
        columnDefinitions.put(EXECTYPE, new ChangeLogColumnDefinition(EXECTYPE, getVarchar(EXECTYPE_SIZE), "EXECUTED", new NotNullConstraint()));
        columnDefinitions.put(LABELS, new ChangeLogColumnDefinition(LABELS, getVarchar(LABELS_SIZE)));
        columnDefinitions.put(CONTEXTS, new ChangeLogColumnDefinition(CONTEXTS, getVarchar(CONTEXTS_SIZE)));

        updateTableSqlStatementProviders.add(new DeploymentColumnStatements(columnDefinitions.get(DEPLOYMENT_ID)));
        updateTableSqlStatementProviders.add(new ShortTextColumnDefinition(columnDefinitions.get(LIQUIBASE)));
        updateTableSqlStatementProviders.add(new ShortTextColumnDefinition(columnDefinitions.get(MD_5_SUM)));
        updateTableSqlStatementProviders.add(new SimpleTextColumnDefinition(columnDefinitions.get(DESCRIPTION)));
        updateTableSqlStatementProviders.add(new SimpleTextColumnDefinition(columnDefinitions.get(TAG)));
        updateTableSqlStatementProviders.add(new SimpleTextColumnDefinition(columnDefinitions.get(COMMENTS)));
        updateTableSqlStatementProviders.add(new ColumnWithDefaultValueSqlStatementProvider(columnDefinitions.get(ORDEREXECUTED)));
        updateTableSqlStatementProviders.add(new ColumnWithDefaultValueSqlStatementProvider(columnDefinitions.get(EXECTYPE)));
        updateTableSqlStatementProviders.add(new SimpleTextColumnIgnoreDifferentTypeDefinition(columnDefinitions.get(LABELS)));
        updateTableSqlStatementProviders.add(new SimpleTextColumnIgnoreDifferentTypeDefinition(columnDefinitions.get(CONTEXTS)));
    }

    public List<AlterChangeLogTableSqlStatementProvider> getUpdateTableSqlStatementProviders() {
        return updateTableSqlStatementProviders;
    }

    private LiquibaseDataType getVarchar(int columnSize) {
        final LiquibaseDataType varchar = new VarcharType();
        varchar.addParameter(columnSize);
        return varchar;
    }

    private LiquibaseDataType getInt() {
        return new IntType();
    }

    public Map<String, ChangeLogColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }
}
