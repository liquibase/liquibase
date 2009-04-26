package liquibase.database.statement;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.InformixDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SybaseASADatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.util.StringUtils;

public class AddPrimaryKeyStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String tablespace;
    private String columnNames;
    private String constraintName;

    public AddPrimaryKeyStatement(String schemaName, String tableName, String columnNames, String constraintName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.constraintName = constraintName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTablespace() {
        return tablespace;
    }

    public AddPrimaryKeyStatement setTablespace(String tablespace) {
        this.tablespace = tablespace;
        return this;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public String getConstraintName() {
        return constraintName;
    }
}
