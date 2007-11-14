package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.MaxDBDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class DropUniqueConstraintStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String constraintName;

    public DropUniqueConstraintStatement(String schemaName, String tableName, String constraintName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.constraintName = constraintName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (database instanceof MySQLDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP KEY " + getConstraintName();
        } else if (database instanceof MaxDBDatabase) {
            return "DROP INDEX " + getConstraintName() + " ON " + database.escapeTableName(getSchemaName(), getTableName());
        }


        return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP CONSTRAINT " + getConstraintName();
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return true;
    }
}
