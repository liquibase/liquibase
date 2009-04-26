package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.database.MaxDBDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SybaseASADatabase;
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
}
