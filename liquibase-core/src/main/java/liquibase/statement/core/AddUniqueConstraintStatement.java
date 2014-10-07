package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;
import liquibase.util.StringUtils;

public class AddUniqueConstraintStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private ColumnConfig[] columns;
    private String constraintName;
    private String tablespace;

    private boolean deferrable;
    private boolean initiallyDeferred;
    private boolean disabled;

    public AddUniqueConstraintStatement(String catalogName, String schemaName, String tableName, ColumnConfig[] columns, String constraintName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columns = columns;
        this.constraintName = constraintName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public ColumnConfig[] getColumns() {
        return columns;
    }

    public String getColumnNames() {
        return StringUtils.join(columns, ", ", new StringUtils.StringUtilsFormatter<ColumnConfig>() {
            @Override
            public String toString(ColumnConfig obj) {
                return obj.getName();
            }
        });    }

    public String getConstraintName() {
        return constraintName;
    }

    public String getTablespace() {
        return tablespace;
    }

    public AddUniqueConstraintStatement setTablespace(String tablespace) {
        this.tablespace = tablespace;
        return this;
    }
    public boolean isDeferrable() {
        return deferrable;
    }

    public AddUniqueConstraintStatement setDeferrable(boolean deferrable) {
        this.deferrable = deferrable;
        return this;
    }

    public boolean isInitiallyDeferred() {
        return initiallyDeferred;
    }

    public AddUniqueConstraintStatement setInitiallyDeferred(boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
        return this;
    }

    public AddUniqueConstraintStatement setDisabled(boolean disabled) {
        this.disabled= disabled;
        return this;
    }

    public boolean isDisabled() {
        return disabled;
    }

}
