package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;
import liquibase.util.StringUtil;

public class AddForeignKeyConstraintStatement extends AbstractSqlStatement {

    private String baseTableCatalogName;
    private String baseTableSchemaName;
    private String baseTableName;
    private ColumnConfig[] baseColumns;

    private String referencedTableCatalogName;
    private String referencedTableSchemaName;
    private String referencedTableName;
    private ColumnConfig[] referencedColumns;

    private String constraintName;

    private boolean deferrable;
    private boolean initiallyDeferred;
    private boolean shouldValidate = true; //only Oracle PL/SQL feature

    private String onDelete;
    private String onUpdate;

    public AddForeignKeyConstraintStatement(String constraintName, String baseTableCatalogName, String baseTableSchemaName,
                                            String baseTableName, ColumnConfig[] baseColumns, String referencedTableCatalogName,
                                            String referencedTableSchemaName, String referencedTableName, ColumnConfig[] referencedColumns) {
        this.baseTableCatalogName = baseTableCatalogName;
        this.baseTableSchemaName = baseTableSchemaName;
        this.baseTableName = baseTableName;
        this.baseColumns = baseColumns;

        this.referencedTableCatalogName = referencedTableCatalogName;
        this.referencedTableSchemaName = referencedTableSchemaName;
        this.referencedTableName = referencedTableName;
        this.referencedColumns = referencedColumns;
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

    public ColumnConfig[] getBaseColumns() {
        return baseColumns;
    }

    public String getBaseColumnNames() {
        return StringUtil.join(baseColumns, ", ", new StringUtil.StringUtilFormatter<ColumnConfig>() {
            @Override
            public String toString(ColumnConfig obj) {
                return obj.getName();
            }
        });
    }

    public String getReferencedTableCatalogName() {
        return referencedTableCatalogName;
    }

    public String getReferencedTableSchemaName() {
        return referencedTableSchemaName;
    }

    public String getReferencedTableName() {
        return referencedTableName;
    }

    public ColumnConfig[] getReferencedColumns() {
        return referencedColumns;
    }

    public String getReferencedColumnNames() {
        return StringUtil.join(referencedColumns, ", ", new StringUtil.StringUtilFormatter<ColumnConfig>() {
            @Override
            public String toString(ColumnConfig obj) {
                return obj.getName();
            }
        });
    }

    public String getConstraintName() {
        return constraintName;
    }

    public boolean isDeferrable() {
        return deferrable;
    }

    public String getOnDelete() {
        return onDelete;
    }

    public String getOnUpdate() {
        return onUpdate;
    }

    public AddForeignKeyConstraintStatement setDeferrable(boolean deferrable) {
        this.deferrable = deferrable;
        return this;
    }

    public boolean isInitiallyDeferred() {
        return initiallyDeferred;
    }

    public AddForeignKeyConstraintStatement setInitiallyDeferred(boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
        return this;
    }

    public AddForeignKeyConstraintStatement setOnUpdate(String updateRule) {
        this.onUpdate = updateRule;
        return this;
    }

    public AddForeignKeyConstraintStatement setOnDelete(String deleteRule) {
        this.onDelete = deleteRule;
        return this;
    }

    /**
     * In Oracle PL/SQL, the VALIDATE keyword defines whether a foreign key constraint on a column in a table
     * should be checked if it refers to a valid row or not.
     * @return true if ENABLE VALIDATE (this is the default), or false if ENABLE NOVALIDATE.
     */
    public boolean shouldValidate() {
        return shouldValidate;
    }

    /**
     * @param shouldValidate - if shouldValidate is set to FALSE then the constraint will be created
     * with the 'ENABLE NOVALIDATE' mode. This means the constraint would be created, but that no
     * check will be done to ensure old data has valid foreign keys - only new data would be checked
     * to see if it complies with the constraint logic. The default state for foreign keys is to
     * have 'ENABLE VALIDATE' set.
     */
    public AddForeignKeyConstraintStatement setShouldValidate(boolean shouldValidate) {
        this.shouldValidate = shouldValidate;
        return this;
    }
}
