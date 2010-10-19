package liquibase.preconditions;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.HashMap;

import liquibase.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.JDBCException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.log.LogFactory;
import liquibase.util.StringUtils;

public class ColumnExistsPrecondition implements Precondition {
    static final protected Logger log = LogFactory.getLogger();
    private String schemaName;
    private String tableName;
    private String columnName;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        HashMap tableColumnNames = new HashMap(3);
        tableColumnNames.put(getTableName(), getColumnName());
        tableColumnNames.put(getTableName().toLowerCase(), getColumnName().toLowerCase());
        tableColumnNames.put(getTableName().toUpperCase(), getColumnName().toUpperCase());

        for (Object tableName : tableColumnNames.keySet()) {
            String columnName = (String) tableColumnNames.get(tableName);
            try {
                if (columnExists(database, database.convertRequestedSchemaToCatalog(getSchemaName()), (String) tableName, columnName)) {
                    return;
                }
            } catch (JDBCException e) {
                throw new PreconditionErrorException(e, changeLog, this);
            } catch (SQLException e) {
                throw new PreconditionErrorException(e, changeLog, this);
            }
        }

        // If we got this far, the table doesn't exist, so throw PreconditionFailedException
        throw new PreconditionFailedException("View "+database.escapeStringForDatabase(getColumnName())+" does not exist", changeLog, this);
    }

    private boolean columnExists(final Database database, final String schemaName, final String tableName, final String columnName)
            throws SQLException {
        // Use DatabaseMetaData to query db's data dictionary
        DatabaseConnection conn = database.getConnection();
        ResultSet columns = null;
        try {
            DatabaseMetaData dbm = conn.getMetaData();
            columns = dbm.getColumns(
                    schemaName,
                    schemaName,
                    tableName,
                    columnName
            );
            return columns.next();
        } finally {
            if (columns != null) {
                try {
                    columns.close();
                } catch (SQLException e) {
                    log.warning("Error closing result set: " + e.getMessage());
                }
            }
        }
    }

    public String getTagName() {
        return "columnExists";
    }
}
