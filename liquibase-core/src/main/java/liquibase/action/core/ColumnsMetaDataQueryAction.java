package liquibase.action.core;

import liquibase.action.MetaDataQueryAction;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtils;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * Action implementation that uses the JDBC MetaData.getColumns() call.
 * Catalog and/or Schema are required if the target database supports it.
 * TableName and columnName can be null, which acts as a "Match Any" filter.
 */
public class ColumnsMetaDataQueryAction extends MetaDataQueryAction {
    private static final String CATALOG_NAME = "catalogName";
    private static final String SCHEMA_NAME = "schemaName";
    private static final String TABLE_NAME = "tableName";
    private static final String COLUMN_NAME = "columnName";

    public ColumnsMetaDataQueryAction(String catalogName, String schemaName, String tableName, String columnName) {
        this.setAttribute(CATALOG_NAME, catalogName);
        this.setAttribute(SCHEMA_NAME, schemaName);
        this.setAttribute(TABLE_NAME, tableName);
        this.setAttribute(COLUMN_NAME, columnName);
    }

    public String getColumnName() {
        return getAttribute(COLUMN_NAME, String.class);
    }

    public String getTableName() {
        return getAttribute(TABLE_NAME, String.class);
    }

    public String getSchemaName() {
        return getAttribute(SCHEMA_NAME, String.class);
    }

    public String getCatalogName() {
        return getAttribute(CATALOG_NAME, String.class);
    }

    protected DatabaseObject rawMetaDataToObject(Map<String, ?> row) {
        Column column = new Column(Table.class, (String) row.get("TABLE_CAT"), (String) row.get("TABLE_SCHEM"), (String) row.get("TABLE_NAME"), (String) row.get("COLUMN_NAME"))
                .setPosition((Integer) row.get("ORDINAL_POSITION"))
                .setRemarks(StringUtils.trimToNull((String) row.get("REMARKS")))
                .setNullable("YES".equals(row.get("IS_NULLABLE")));
        if ("YES".equals(row.get("IS_AUTOINCREMENT"))) {
            column.setAutoIncrementInformation(new Column.AutoIncrementInformation());
        }

        return column;
    }

    @Override
    protected QueryResult getRawMetaData(ExecutionOptions options) throws DatabaseException {
        DatabaseMetaData metaData = ((JdbcConnection) options.getRuntimeEnvironment().getTargetDatabase().getConnection()).getMetaData();

        try {
            return new QueryResult(JdbcUtils.extract(metaData.getColumns(
                    getCatalogName(),
                    getSchemaName(),
                    getTableName(),
                    getColumnName())));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}