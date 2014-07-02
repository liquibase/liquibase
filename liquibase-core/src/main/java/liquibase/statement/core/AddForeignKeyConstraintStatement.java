package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;

/**
 * Adds a foreign key constraint to an existing column
 */
public class AddForeignKeyConstraintStatement extends AbstractStatement {

    private static final String BASE_TABLE_CATALOG_NAME = "baseTableCatalogName";
    private static final String BASE_TABLE_SCHEMA_NAME = "baseTableSchemaName";
    private static final String BASE_TABLE_NAME = "baseTableName";
    private static final String BASE_COLUMN_NAMES = "baseColumnNames";

    private static final String REFERENCED_TABLE_CATALOG_NAME = "referencedTableCatalogName";
    private static final String REFERENCED_TABLE_SCHEMA_NAME = "referencedTableSchemaName";
    private static final String REFERENCED_TABLE_NAME = "referencedTableName";
    private static final String REFERENCED_COLUMN_NAMES = "referencedColumnNames";

    private static final String CONSTRAINT_NAME = "constraintName";

    private static final String DEFERRABLE = "deferrable";
    private static final String INITIALLY_DEFERRED = "initiallyDeferred";

    private static final String ON_DELETE = "onDelete";
    private static final String ON_UPDATE = "onUpdate";

    public AddForeignKeyConstraintStatement() {
    }

    public AddForeignKeyConstraintStatement(String constraintName, String baseTableCatalogName, String baseTableSchemaName, String baseTableName, String baseColumnNames, String referencedTableCatalogName, String referencedTableSchemaName, String referencedTableName, String referencedColumnNames) {
        setBaseTableCatalogName(baseTableCatalogName);
        setBaseTableSchemaName(baseTableSchemaName);
        setBaseTableName(baseTableName);
        setBaseColumnNames(baseColumnNames);

        setReferencedTableCatalogName(referencedTableCatalogName);
        setReferencedTableSchemaName(referencedTableSchemaName);
        setReferencedTableName(referencedTableName);
        setReferencedColumnNames(referencedColumnNames);
        setConstraintName(constraintName);
    }

    public String getBaseTableCatalogName() {
        return getAttribute(BASE_TABLE_CATALOG_NAME, String.class);
    }

    public AddForeignKeyConstraintStatement setBaseTableCatalogName(String baseTableCatalogName) {
        return (AddForeignKeyConstraintStatement) setAttribute(BASE_TABLE_CATALOG_NAME, baseTableCatalogName);
    }

    public String getBaseTableSchemaName() {
        return getAttribute(BASE_TABLE_SCHEMA_NAME, String.class);
    }

    public AddForeignKeyConstraintStatement setBaseTableSchemaName(String baseTableSchemaName) {
        return (AddForeignKeyConstraintStatement) setAttribute(BASE_TABLE_SCHEMA_NAME, baseTableSchemaName);
    }


    public String getBaseTableName() {
        return getAttribute(BASE_TABLE_NAME, String.class);
    }

    public AddForeignKeyConstraintStatement setBaseTableName(String baseTableName) {
        return (AddForeignKeyConstraintStatement) setAttribute(BASE_TABLE_NAME, baseTableName);
    }


    public String getBaseColumnNames() {
        return getAttribute(BASE_COLUMN_NAMES, String.class);
    }

    public AddForeignKeyConstraintStatement setBaseColumnNames(String baseColumnNames) {
        return (AddForeignKeyConstraintStatement) setAttribute(BASE_COLUMN_NAMES, baseColumnNames);
    }


    public String getReferencedTableCatalogName() {
        return getAttribute(REFERENCED_TABLE_CATALOG_NAME, String.class);
    }

    public AddForeignKeyConstraintStatement setReferencedTableCatalogName(String referencedTableCatalogName) {
        return (AddForeignKeyConstraintStatement) setAttribute(REFERENCED_TABLE_CATALOG_NAME, referencedTableCatalogName);
    }


    public String getReferencedTableSchemaName() {
        return getAttribute(REFERENCED_TABLE_SCHEMA_NAME, String.class);
    }

    public AddForeignKeyConstraintStatement setReferencedTableSchemaName(String referencedTableSchemaName) {
        return (AddForeignKeyConstraintStatement) setAttribute(REFERENCED_TABLE_SCHEMA_NAME, referencedTableSchemaName);
    }


    public String getReferencedTableName() {
        return getAttribute(REFERENCED_TABLE_NAME, String.class);
    }

    public AddForeignKeyConstraintStatement setReferencedTableName(String referencedTableName) {
        return (AddForeignKeyConstraintStatement) setAttribute(REFERENCED_TABLE_NAME, referencedTableName);
    }


    public String getReferencedColumnNames() {
        return getAttribute(REFERENCED_COLUMN_NAMES, String.class);
    }

    public AddForeignKeyConstraintStatement setReferencedColumnNames(String referencedColumnNames) {
        return (AddForeignKeyConstraintStatement) setAttribute(REFERENCED_COLUMN_NAMES, referencedColumnNames);
    }


    public String getConstraintName() {
        return getAttribute(CONSTRAINT_NAME, String.class);
    }

    public AddForeignKeyConstraintStatement setConstraintName(String constraintName) {
        return (AddForeignKeyConstraintStatement) setAttribute(CONSTRAINT_NAME, constraintName);
    }


    public boolean isDeferrable() {
        return getAttribute(DEFERRABLE, false);
    }

    public AddForeignKeyConstraintStatement setDeferrable(boolean deferrable) {
        return (AddForeignKeyConstraintStatement) setAttribute(DEFERRABLE, deferrable);
    }

    public String getOnDelete() {
        return getAttribute(ON_DELETE, String.class);
    }

    public AddForeignKeyConstraintStatement setOnDelete(String deleteRule) {
        return (AddForeignKeyConstraintStatement) setAttribute(ON_DELETE, deleteRule);
    }


    public String getOnUpdate() {
        return getAttribute(ON_UPDATE, String.class);
    }

    public AddForeignKeyConstraintStatement setOnUpdate(String updateRule) {
        return (AddForeignKeyConstraintStatement) setAttribute(ON_UPDATE, updateRule);
    }


    public boolean isInitiallyDeferred() {
        return getAttribute(INITIALLY_DEFERRED, false);
    }

    public AddForeignKeyConstraintStatement setInitiallyDeferred(boolean initiallyDeferred) {
        return (AddForeignKeyConstraintStatement) setAttribute(INITIALLY_DEFERRED, initiallyDeferred);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new ForeignKey().setName(getConstraintName()).setForeignKeyColumns(getBaseColumnNames()).setForeignKeyTable((Table) new Table().setName(getBaseTableName()).setSchema(getBaseTableCatalogName(), getBaseTableSchemaName()))
        };
    }
}
