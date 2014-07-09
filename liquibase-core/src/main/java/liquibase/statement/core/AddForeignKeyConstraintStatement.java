package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;

/**
 * Adds a foreign key constraint to an existing column
 */
public class AddForeignKeyConstraintStatement extends AbstractForeignKeyStatement {

    public static final String BASE_COLUMN_NAMES = "baseColumnNames";

    public static final String REFERENCED_TABLE_CATALOG_NAME = "referencedTableCatalogName";
    public static final String REFERENCED_TABLE_SCHEMA_NAME = "referencedTableSchemaName";
    public static final String REFERENCED_TABLE_NAME = "referencedTableName";
    public static final String REFERENCED_COLUMN_NAMES = "referencedColumnNames";

    public static final String DEFERRABLE = "deferrable";
    public static final String INITIALLY_DEFERRED = "initiallyDeferred";

    public static final String ON_DELETE = "onDelete";
    public static final String ON_UPDATE = "onUpdate";

    public AddForeignKeyConstraintStatement() {
    }

    public AddForeignKeyConstraintStatement(String constraintName, String baseTableCatalogName, String baseTableSchemaName, String baseTableName, String baseColumnNames, String referencedTableCatalogName, String referencedTableSchemaName, String referencedTableName, String referencedColumnNames) {
        super(constraintName, baseTableCatalogName, baseTableSchemaName, baseTableName);

        setBaseColumnNames(baseColumnNames);

        setReferencedTableCatalogName(referencedTableCatalogName);
        setReferencedTableSchemaName(referencedTableSchemaName);
        setReferencedTableName(referencedTableName);
        setReferencedColumnNames(referencedColumnNames);
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
