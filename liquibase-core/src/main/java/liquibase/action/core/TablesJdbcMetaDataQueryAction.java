package liquibase.action.core;

import liquibase.action.MetaDataQueryAction;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import  liquibase.ExecutionEnvironment;
import liquibase.executor.QueryResult;
import liquibase.executor.Row;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtils;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Action implementation that uses the JDBC MetaData.getTables() call.
 * Catalog and/or Schema are required if the target database supports it.
 * TableName can be null, which acts as a "Match Any" filter.
 * No changes in case are made to any object names, they must be corrected as needed before creating this object.
 */
public class TablesJdbcMetaDataQueryAction extends MetaDataQueryAction {

    Logger log = LogFactory.getInstance().getLog();

    private static final String CATALOG_NAME = "catalogName";
    private static final String SCHEMA_NAME = "schemaName";
    private static final String TABLE_NAME = "tableName";

    public TablesJdbcMetaDataQueryAction(String catalogName, String schemaName, String tableName) {
        this.setAttribute(CATALOG_NAME, catalogName);
        this.setAttribute(SCHEMA_NAME, schemaName);
        this.setAttribute(TABLE_NAME, tableName);
    }

    protected DatabaseObject rawMetaDataToObject(Row row, ExecutionEnvironment env) {
        return new Table(row.get("TABLE_CAT", String.class), row.get("TABLE_SCHEM", String.class), row.get("TABLE_NAME", String.class))
                .setRemarks(StringUtils.trimToNull(row.get("REMARKS", String.class)));
    }

    @Override
    protected QueryResult getRawMetaData(ExecutionEnvironment env) throws DatabaseException {
        DatabaseMetaData metaData = ((JdbcConnection) env.getTargetDatabase().getConnection()).getMetaData();

        log.debug("QUERY: metaData.getTables("+getAttribute(CATALOG_NAME, String.class)+", "+getAttribute(SCHEMA_NAME, String.class)+", "+getAttribute(TABLE_NAME, String.class)+", new String[]{\"TABLE\"})");
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
