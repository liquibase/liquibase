package liquibase.database.sql;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.log.LogFactory;

import java.util.logging.Logger;

public class DropTableStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private boolean cascadeConstraints;

    public DropTableStatement(String schemaName, String tableName, boolean cascadeConstraints) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.cascadeConstraints = cascadeConstraints;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isCascadeConstraints() {
        return cascadeConstraints;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        StringBuffer buffer = new StringBuffer();
        buffer.append("DROP TABLE ").append(database.escapeTableName(getSchemaName(), getTableName()));
        if (isCascadeConstraints()) {
            if (database instanceof DerbyDatabase
                    || database instanceof DB2Database
                    || database instanceof MSSQLDatabase
                    || database instanceof FirebirdDatabase
                    || database instanceof SQLiteDatabase) {
                LogFactory.getLogger().info("Database does not support drop with cascade");
            } else if (database instanceof OracleDatabase) {
                buffer.append(" CASCADE CONSTRAINTS");
            } else {
                buffer.append(" CASCADE");
            }
        }

        return buffer.toString();

    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return true;
    }
}
