package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;
import liquibase.util.StringUtils;

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

    private String onDelete;
    private String onUpdate;

    public AddForeignKeyConstraintStatement(String constraintName, String baseTableCatalogName, String baseTableSchemaName, String baseTableName, ColumnConfig[] baseColumns, String referencedTableCatalogName, String referencedTableSchemaName, String referencedTableName, ColumnConfig[] referencedColumns) {
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
        return StringUtils.join(baseColumns, ", ", new StringUtils.StringUtilsFormatter<ColumnConfig>() {
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
        return StringUtils.join(referencedColumns, ", ", new StringUtils.StringUtilsFormatter<ColumnConfig>() {
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
}
