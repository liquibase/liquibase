package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;
import liquibase.util.StringUtil;
import lombok.Getter;

public class AddForeignKeyConstraintStatement extends AbstractSqlStatement {

    @Getter
    private final String baseTableCatalogName;
    @Getter
    private final String baseTableSchemaName;
    @Getter
    private final String baseTableName;
    @Getter
    private final ColumnConfig[] baseColumns;

    @Getter
    private final String referencedTableCatalogName;
    @Getter
    private final String referencedTableSchemaName;
    @Getter
    private final String referencedTableName;
    @Getter
    private final ColumnConfig[] referencedColumns;

    @Getter
    private final String constraintName;

    @Getter
    private boolean deferrable;
    @Getter
    private boolean initiallyDeferred;
    private boolean shouldValidate = true; //only Oracle PL/SQL feature

    @Getter
    private String onDelete;
    @Getter
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

    public String getBaseColumnNames() {
        return StringUtil.join(baseColumns, ", ", (StringUtil.StringUtilFormatter<ColumnConfig>) ColumnConfig::getName);
    }

    public String getReferencedColumnNames() {
        return StringUtil.join(referencedColumns, ", ", (StringUtil.StringUtilFormatter<ColumnConfig>) ColumnConfig::getName);
    }

    public AddForeignKeyConstraintStatement setDeferrable(boolean deferrable) {
        this.deferrable = deferrable;
        return this;
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
