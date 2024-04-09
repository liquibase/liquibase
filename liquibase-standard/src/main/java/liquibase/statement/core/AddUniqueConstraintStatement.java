package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;
import liquibase.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

public class AddUniqueConstraintStatement extends AbstractSqlStatement {

    @Getter
    private final String catalogName;
    @Getter
    private final String schemaName;
    @Getter
    private final String tableName;
    @Getter
    private final ColumnConfig[] columns;
    @Getter
    private final String constraintName;
    @Getter
    private String tablespace;

    @Getter
    private boolean clustered;
    private boolean shouldValidate = true; //only Oracle PL/SQL feature

    @Getter
    private boolean deferrable;
    @Getter
    private boolean initiallyDeferred;
    @Getter
    private boolean disabled;

    @Getter
    @Setter
    private String forIndexName;
    @Getter
    @Setter
    private String forIndexSchemaName;
    @Getter
    @Setter
    private String forIndexCatalogName;

    public AddUniqueConstraintStatement(String catalogName, String schemaName, String tableName, ColumnConfig[] columns, String constraintName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columns = columns;
        this.constraintName = constraintName;
    }

    public String getColumnNames() {
        return StringUtil.join(columns, ", ", (StringUtil.StringUtilFormatter<ColumnConfig>) obj -> obj.getName() + (obj.getDescending() != null && obj.getDescending() ? " DESC" : ""));
    }

    public AddUniqueConstraintStatement setTablespace(String tablespace) {
        this.tablespace = tablespace;
        return this;
    }

    public AddUniqueConstraintStatement setDeferrable(boolean deferrable) {
        this.deferrable = deferrable;
        return this;
    }

    public AddUniqueConstraintStatement setInitiallyDeferred(boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
        return this;
    }

    public AddUniqueConstraintStatement setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public AddUniqueConstraintStatement setClustered(boolean clustered) {
        this.clustered= clustered;
        return this;
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
