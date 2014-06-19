package liquibase.action.metadata;

import liquibase.action.MetaDataQueryAction;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

public class FetchColumnsAction extends MetaDataQueryAction {
    private static final String CATALOG_NAME = "catalogName";
    private static final String SCHEMA_NAME = "schemaName";
    private static final String TABLE_NAME = "tableName";
    private static final String COLUMN_NAME = "columnName";

    public FetchColumnsAction(String catalogName, String schemaName, String tableName, String columnName) {
        this.setAttribute(CATALOG_NAME, catalogName);
        this.setAttribute(SCHEMA_NAME, schemaName);
        this.setAttribute(TABLE_NAME, tableName);
        this.setAttribute(COLUMN_NAME, columnName);
    }

    protected DatabaseObject rawMetaDataToObject(Map<String, ?> row) {
        return new Column(Table.class, (String) row.get("TABLE_CAT"), (String) row.get("TABLE_SCHEM"), (String) row.get("TABLE_NAME"), (String) row.get("COLUMN_NAME"));
    }

    @Override
    protected QueryResult getRawMetaData(ExecutionOptions options) throws DatabaseException {
        DatabaseMetaData metaData = ((JdbcConnection) options.getRuntimeEnvironment().getTargetDatabase().getConnection()).getMetaData();

        try {
            return new QueryResult(JdbcUtils.extract(metaData.getColumns(
                    getAttribute(CATALOG_NAME, String.class),
                    getAttribute(SCHEMA_NAME, String.class),
                    getAttribute(TABLE_NAME, String.class),
                    getAttribute(COLUMN_NAME, String.class))));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}