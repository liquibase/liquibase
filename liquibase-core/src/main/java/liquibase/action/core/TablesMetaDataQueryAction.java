package liquibase.action.core;

import liquibase.action.MetaDataQueryAction;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtils;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * Action implementation that uses the JDBC MetaData.getTables() call.
 * Catalog and/or Schema are required if the target database supports it.
 * TableName can be null, which acts as a "Match Any" filter.
 */
public class TablesMetaDataQueryAction extends MetaDataQueryAction {

    private static final String CATALOG_NAME = "catalogName";
    private static final String SCHEMA_NAME = "schemaName";
    private static final String TABLE_NAME = "tableName";

    public TablesMetaDataQueryAction(String catalogName, String schemaName, String tableName) {
        this.setAttribute(CATALOG_NAME, catalogName);
        this.setAttribute(SCHEMA_NAME, schemaName);
        this.setAttribute(TABLE_NAME, tableName);
    }

    protected DatabaseObject rawMetaDataToObject(Map<String, ?> row) {
        return new Table((String) row.get("TABLE_CAT"), (String) row.get("TABLE_SCHEM"), (String) row.get("TABLE_NAME"))
                .setRemarks(StringUtils.trimToNull((String) row.get("REMARKS")));
    }

    @Override
    protected QueryResult getRawMetaData(ExecutionOptions options) throws DatabaseException {
        DatabaseMetaData metaData = ((JdbcConnection) options.getRuntimeEnvironment().getTargetDatabase().getConnection()).getMetaData();

        try {
            return new QueryResult(JdbcUtils.extract(metaData.getTables(
                    getAttribute(CATALOG_NAME, String.class),
                    getAttribute(SCHEMA_NAME, String.class),
                    getAttribute(TABLE_NAME, String.class), new String[]{"TABLE"})));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
