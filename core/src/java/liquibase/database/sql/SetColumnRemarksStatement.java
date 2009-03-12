package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class SetColumnRemarksStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String columnName;
    private String remarks;

    public SetColumnRemarksStatement(String schemaName, String tableName, String columnName, String remarks) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.remarks = remarks;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        return "COMMENT ON COLUMN "+database.escapeTableName(schemaName, tableName)+"."+database.escapeColumnName(schemaName, tableName, columnName)+" IS '"+remarks+"'";
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return database instanceof OracleDatabase;
    }
}
