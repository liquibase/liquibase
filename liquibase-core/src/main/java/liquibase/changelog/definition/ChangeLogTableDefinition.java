package liquibase.changelog.definition;

import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.DateTimeType;
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
    private static final String ID = "ID";
    private static final String AUTHOR = "AUTHOR";
    private static final String FILENAME = "FILENAME";
    private static final String DATEEXECUTED = "DATEEXECUTED";

    private List<AlterChangeLogTableSqlStatementProvider> updateTableSqlStatementProviders = new ArrayList<>();
    private Map<String, ChangeLogColumnDefinition> columnDefinitions = new HashMap<>();

    public ChangeLogTableDefinition() {
        addColumn(new ChangeLogColumnDefinition(ID, getVarchar(255), null, new NotNullConstraint()));
        addColumn(new ChangeLogColumnDefinition(AUTHOR, getVarchar(255), null, new NotNullConstraint()));
        addColumn(new ChangeLogColumnDefinition(FILENAME, getVarchar(255), null, new NotNullConstraint()));
        addColumn(new ChangeLogColumnDefinition(DATEEXECUTED, getDateTime(), null, new NotNullConstraint()));
        addColumn(new ChangeLogColumnDefinition(ORDEREXECUTED, getInt(), ORDEREXECUTED_DEFAULT_VALUE, new NotNullConstraint()), ColumnWithDefaultValueSqlStatementProvider.class);
        addColumn(new ChangeLogColumnDefinition(DEPLOYMENT_ID, getVarchar(DEPLOYMENT_COLUMN_SIZE)), DeploymentColumnStatements.class);
        addColumn(new ChangeLogColumnDefinition(LIQUIBASE, getVarchar(LIQUIBASE_SIZE)), ShortTextColumnDefinition.class);
        addColumn(new ChangeLogColumnDefinition(MD_5_SUM, getVarchar(MD5SUM_SIZE)), ShortTextColumnDefinition.class);
        addColumn(new ChangeLogColumnDefinition(DESCRIPTION, getVarchar(DESCRIPTON_SIZE)), SimpleTextColumnDefinition.class);
        addColumn(new ChangeLogColumnDefinition(TAG, getVarchar(TAG_SIZE)), SimpleTextColumnDefinition.class);
        addColumn(new ChangeLogColumnDefinition(COMMENTS, getVarchar(COMMENTS_SIZE)), SimpleTextColumnDefinition.class);
        addColumn(new ChangeLogColumnDefinition(EXECTYPE, getVarchar(EXECTYPE_SIZE), "EXECUTED", new NotNullConstraint()), ColumnWithDefaultValueSqlStatementProvider.class);
        addColumn(new ChangeLogColumnDefinition(LABELS, getVarchar(LABELS_SIZE)), SimpleTextColumnIgnoreDifferentTypeDefinition.class);
        addColumn(new ChangeLogColumnDefinition(CONTEXTS, getVarchar(CONTEXTS_SIZE)), SimpleTextColumnIgnoreDifferentTypeDefinition.class);
    }

    private DateTimeType getDateTime() {
        return new DateTimeType();
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

    public void addColumn(ChangeLogColumnDefinition definition) {
        columnDefinitions.put(definition.getColumnName(), definition);
    }

    public void addColumn(ChangeLogColumnDefinition definition, Class<? extends AlterChangeLogTableSqlStatementProvider> alterProviderClass) {
        try {
            updateTableSqlStatementProviders.add(alterProviderClass.getConstructor(ChangeLogColumnDefinition.class).newInstance(definition));
            columnDefinitions.put(definition.getColumnName(), definition);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
