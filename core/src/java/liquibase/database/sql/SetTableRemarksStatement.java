package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.PostgresDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class SetTableRemarksStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String remarks;

    public SetTableRemarksStatement(String schemaName, String tableName, String remarks) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.remarks = remarks;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (database instanceof OracleDatabase || database instanceof PostgresDatabase) {
            return "COMMENT ON TABLE "+database.escapeTableName(schemaName, tableName)+" IS '"+remarks.replace("'", "''")+"'";
        } else {
            return "ALTER TABLE "+database.escapeTableName(schemaName, tableName)+" COMMENT = '"+remarks.replace("'", "''")+"'";
        }
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return database instanceof MySQLDatabase || database instanceof OracleDatabase || database instanceof PostgresDatabase;
    }
}
