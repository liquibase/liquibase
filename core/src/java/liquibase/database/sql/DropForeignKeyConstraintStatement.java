package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class DropForeignKeyConstraintStatement implements SqlStatement {

    private String baseTableSchemaName;
    private String baseTableName;
    private String constraintName;

    public DropForeignKeyConstraintStatement(String baseTableSchemaName, String baseTableName, String constraintName) {
        this.baseTableSchemaName = baseTableSchemaName;
        this.baseTableName = baseTableName;
        this.constraintName = constraintName;
    }

    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    public String getBaseTableName() {
        return baseTableName;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (getBaseTableSchemaName() != null && !database.supportsSchemas()) {
            throw new StatementNotSupportedOnDatabaseException("Database does not support schemas", this, database);
        }
        
        if (database instanceof MySQLDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getBaseTableSchemaName(), getBaseTableName()) + " DROP FOREIGN KEY " + getConstraintName();
        } else {
            return "ALTER TABLE " + database.escapeTableName(getBaseTableSchemaName(), getBaseTableName()) + " DROP CONSTRAINT " + getConstraintName();
        }
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return true;
    }
}
