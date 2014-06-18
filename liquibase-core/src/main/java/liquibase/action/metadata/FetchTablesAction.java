package liquibase.action.metadata;

import liquibase.action.MetaDataQueryAction;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

public class FetchTablesAction extends MetaDataQueryAction {

    private static final String CATALOG_NAME = "catalogName";
    private static final String SCHEMA_NAME = "schemaName";
    private static final String TABLE_NAME = "tableName";

    public FetchTablesAction(String catalogName, String schemaName, String tableName) {
        this.setAttribute(CATALOG_NAME, catalogName);
        this.setAttribute(SCHEMA_NAME, schemaName);
        this.setAttribute(TABLE_NAME, tableName);
    }

    protected DatabaseObject createObject(Map<String, ?> row) {
        return new Table((String) row.get("TABLE_CAT"), (String) row.get("TABLE_SCHEM"), (String) row.get("TABLE_NAME"));
    }

    @Override
    protected QueryResult executeQuery(ExecutionOptions options) throws DatabaseException {
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
