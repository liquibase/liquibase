package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class FindForeignKeyConstraintsStatement implements SqlStatement {

    public static final String RESULT_COLUMN_BASE_TABLE_NAME        = "TABLE_NAME";
    public static final String RESULT_COLUMN_BASE_TABLE_COLUMN_NAME = "COLUMN_NAME";
    public static final String RESULT_COLUMN_FOREIGN_TABLE_NAME     = "REFERENCED_TABLE_NAME";
    public static final String RESULT_COLUMN_FOREIGN_COLUMN_NAME    = "REFERENCED_COLUMN_NAME";
    public static final String RESULT_COLUMN_CONSTRAINT_NAME        = "CONSTRAINT_NAME";

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