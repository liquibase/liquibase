package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropForeignKeyConstraintStatement extends AbstractSqlStatement {

    private final String baseTableCatalogName;
    private final String baseTableSchemaName;
    private final String baseTableName;
    private final String constraintName;

    public DropForeignKeyConstraintStatement(String baseTableCatalogName, String baseTableSchemaName, String baseTableName, String constraintName) {
        this.baseTableCatalogName = baseTableCatalogName;
        this.baseTableSchemaName = baseTableSchemaName;
        this.baseTableName = baseTableName;
        this.constraintName = constraintName;
    }

    public String getBaseTableCatalogName() {
        return baseTableCatalogName;
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
