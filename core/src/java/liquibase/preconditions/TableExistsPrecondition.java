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
        // Use DatabaseMetaData to query db's data dictionary
        DatabaseConnection conn = database.getConnection();
        ResultSet tables = null;
        try {
            DatabaseMetaData dbm = conn.getMetaData();
            tables = dbm.getTables(
                    database.convertRequestedSchemaToCatalog(getSchemaName()),
                    database.convertRequestedSchemaToSchema(getSchemaName()),
                    getTableName(),
                    new String[]{"TABLE"}
            );
            if (!tables.next()) {
                throw new PreconditionFailedException("Table "+database.escapeTableName(getSchemaName(), getTableName())+" does not exist", changeLog, this);
            }
        } catch (JDBCException je) {
            throw new PreconditionErrorException(je, changeLog, this);
        } catch (SQLException se) {
            throw new PreconditionErrorException(se, changeLog, this);
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
