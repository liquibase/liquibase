package liquibase.preconditions;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.DatabaseChangeLog;
import liquibase.util.StringUtils;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.JDBCException;

public class TableExistsPrecondition implements Precondition {
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
        Connection conn = database.getConnection().getUnderlyingConnection();
        try {
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, schemaName, database.escapeTableName(getSchemaName(), getTableName()), null);
            if (!tables.next()) {
                throw new PreconditionFailedException("Table "+database.escapeTableName(getSchemaName(), getTableName())+" does not exist", changeLog, this);
            }
        } catch (SQLException se) {
            throw new PreconditionErrorException(se, changeLog, this);
        }
    }

    public String getTagName() {
        return "tableExists";
    }
}
