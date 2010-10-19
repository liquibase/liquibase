package liquibase.preconditions;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import liquibase.DatabaseChangeLog;
import liquibase.log.LogFactory;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.JDBCException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.util.StringUtils;

public class TableExistsPrecondition implements Precondition {
    static final protected Logger log = LogFactory.getLogger();
    
    private String schemaName;
    private String tableName;

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

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        String[] tableNames = new String[3];
        tableNames[0] = getTableName();
        tableNames[1] = getTableName().toLowerCase();
        tableNames[2] = getTableName().toUpperCase();

        for (String tableName : tableNames) {
            try {
                if (tableExists(database, database.convertRequestedSchemaToCatalog(getSchemaName()), tableName)) {
                    return;
                }
            } catch (JDBCException e) {
                throw new PreconditionErrorException(e, changeLog, this);
            } catch (SQLException e) {
                throw new PreconditionErrorException(e, changeLog, this);
            }
        }

        // If we got this far, the table doesn't exist, so throw PreconditionFailedException
        throw new PreconditionFailedException("Table "+database.escapeTableName(getSchemaName(), getTableName())+" does not exist", changeLog, this);
    }

    private boolean tableExists(final Database database, final String schemaName, final String tableName) throws SQLException {
        // Use DatabaseMetaData to query db's data dictionary
        DatabaseConnection conn = database.getConnection();
        ResultSet tables = null;
        try {
            DatabaseMetaData dbm = conn.getMetaData();
            tables = dbm.getTables(
                    schemaName,
                    schemaName,
                    tableName,
                    new String[]{"TABLE"}
            );
            return tables.next();
        } finally {
            if (tables != null) {
                try {
                    tables.close();
                } catch (SQLException e) {
                    log.warning("Error closing result set: " + e.getMessage());
                }
            }
        }
    }

    public String getTagName() {
        return "tableExists";
    }
}
