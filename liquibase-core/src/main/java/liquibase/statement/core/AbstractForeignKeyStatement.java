package liquibase.statement.core;

import liquibase.statement.AbstractStatement;

public abstract class AbstractForeignKeyStatement extends AbstractStatement {

    private static final String BASE_TABLE_CATALOG_NAME = "baseTableCatalogName";
    private static final String BASE_TABLE_SCHEMA_NAME = "baseTableSchemaName";
    private static final String BASE_TABLE_NAME = "baseTableName";

    private static final String CONSTRAINT_NAME = "constraintName";

    protected AbstractForeignKeyStatement() {
    }

    public AbstractForeignKeyStatement(String constraintName, String baseTableCatalogName, String baseTableSchemaName, String baseTableName) {
        setBaseTableCatalogName(baseTableCatalogName);
        setBaseTableSchemaName(baseTableSchemaName);
        setBaseTableName(baseTableName);
        setConstraintName(constraintName);

    }

    public String getBaseTableCatalogName() {
        return getAttribute(BASE_TABLE_CATALOG_NAME, String.class);
    }

    public AbstractForeignKeyStatement setBaseTableCatalogName(String baseTableCatalogName) {
        return (AbstractForeignKeyStatement) setAttribute(BASE_TABLE_CATALOG_NAME, baseTableCatalogName);
    }

    public String getBaseTableSchemaName() {
        return getAttribute(BASE_TABLE_SCHEMA_NAME, String.class);
    }

    public AbstractForeignKeyStatement setBaseTableSchemaName(String baseTableSchemaName) {
        return (AbstractForeignKeyStatement) setAttribute(BASE_TABLE_SCHEMA_NAME, baseTableSchemaName);
    }


    public String getBaseTableName() {
        return getAttribute(BASE_TABLE_NAME, String.class);
    }

    public AbstractForeignKeyStatement setBaseTableName(String baseTableName) {
        return (AbstractForeignKeyStatement) setAttribute(BASE_TABLE_NAME, baseTableName);
    }

    public String getConstraintName() {
        return getAttribute(CONSTRAINT_NAME, String.class);
    }

    public AbstractForeignKeyStatement setConstraintName(String constraintName) {
        return (AbstractForeignKeyStatement) setAttribute(CONSTRAINT_NAME, constraintName);
    }




}
