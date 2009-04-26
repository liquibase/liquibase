package liquibase.database.statement;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class FindForeignKeyConstraintsStatement implements SqlStatement {

    public static final String RESULT_COLUMN_BASE_TABLE_NAME        = "k_table";
    public static final String RESULT_COLUMN_BASE_TABLE_COLUMN_NAME = "fk_column";
    public static final String RESULT_COLUMN_FOREIGN_TABLE_NAME     = "pk_table";
    public static final String RESULT_COLUMN_FOREIGN_COLUMN_NAME    = "pk_column";
    public static final String RESULT_COLUMN_CONSTRAINT_NAME        = "constraint_name";

    private String baseTableSchemaName;
    private String baseTableName;

    public FindForeignKeyConstraintsStatement(String baseTableSchemaName, String baseTableName) {
        this.baseTableSchemaName = baseTableSchemaName;
        this.baseTableName = baseTableName;
    }

    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    public void setBaseTableSchemaName(String baseTableSchemaName) {
        this.baseTableSchemaName = baseTableSchemaName;
    }

    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }
}