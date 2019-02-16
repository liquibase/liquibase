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

    private boolean clustered;
    private boolean shouldValidate = true; //only Oracle PL/SQL feature

    private boolean deferrable;
    private boolean initiallyDeferred;
    private boolean disabled;

    private String forIndexName;
    private String forIndexSchemaName;
    private String forIndexCatalogName;

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
                return obj.getName() + (obj.getDescending() != null && obj.getDescending() ? " DESC" : "");
            }
        });
    }

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

    public boolean isDisabled() {
        return disabled;
    }

    public AddUniqueConstraintStatement setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public AddUniqueConstraintStatement setClustered(boolean clustered) {
        this.clustered= clustered;
        return this;
    }

    public boolean isClustered() {
        return clustered;
    }

    public String getForIndexName() {
        return forIndexName;
    }

    public void setForIndexName(String forIndexName) {
        this.forIndexName = forIndexName;
    }

    public String getForIndexSchemaName() {
        return forIndexSchemaName;
    }

    public void setForIndexSchemaName(String forIndexSchemaName) {
        this.forIndexSchemaName = forIndexSchemaName;
    }

    public String getForIndexCatalogName() {
        return forIndexCatalogName;
    }

    public void setForIndexCatalogName(String forIndexCatalogName) {
        this.forIndexCatalogName = forIndexCatalogName;
    }

    /**
     * In Oracle PL/SQL, the VALIDATE keyword defines whether a newly added unique constraint on a 
     * column in a table should cause existing rows to be checked to see if they satisfy the 
     * uniqueness constraint or not. 
     * @return true if ENABLE VALIDATE (this is the default), or false if ENABLE NOVALIDATE.
     */
    public boolean shouldValidate() {
        return shouldValidate;
    }

    /**
     * @param shouldValidate - if shouldValidate is set to FALSE then the constraint will be created
     * with the 'ENABLE NOVALIDATE' mode. This means the constraint would be created, but that no
     * check will be done to ensure old data has valid constraints - only new data would be checked
     * to see if it complies with the constraint logic. The default state for unique constraints is to
     * have 'ENABLE VALIDATE' set.
     */
    public AddUniqueConstraintStatement setShouldValidate(boolean shouldValidate) {
        this.shouldValidate = shouldValidate;
        return this;
    }
}
