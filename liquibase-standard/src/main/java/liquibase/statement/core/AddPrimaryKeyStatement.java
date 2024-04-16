package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;
import liquibase.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

public class AddPrimaryKeyStatement extends AbstractSqlStatement {

    @Getter
    private final String catalogName;
    @Getter
    private final String schemaName;
    @Getter
    private final String tableName;
    @Getter
    private String tablespace;
    @Getter
    private final ColumnConfig[] columns;
    @Getter
    private final String constraintName;
    private Boolean clustered;

    @Getter
    @Setter
    private String forIndexName;
    @Getter
    @Setter
    private String forIndexSchemaName;
    @Getter
    @Setter
    private String forIndexCatalogName;
    private boolean shouldValidate = true;

    /**
     * @deprecated
     */
    public AddPrimaryKeyStatement(String catalogName, String schemaName, String tableName, String columnNames, String constraintName) {
        this(catalogName, schemaName, tableName, ColumnConfig.arrayFromNames(columnNames), constraintName);
    }

    public AddPrimaryKeyStatement(String catalogName, String schemaName, String tableName, ColumnConfig[] columns, String constraintName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columns = columns;
        this.constraintName = constraintName;
    }

    public AddPrimaryKeyStatement setTablespace(String tablespace) {
        this.tablespace = tablespace;
        return this;
    }

    public String getColumnNames() {
        return StringUtil.join(columns, ", ", (StringUtil.StringUtilFormatter<ColumnConfig>) obj -> obj.getName() + (obj.getDescending() != null && obj.getDescending() ? " DESC" : ""));
    }

    public Boolean isClustered() {
        return clustered;
    }

    public AddPrimaryKeyStatement setClustered(Boolean clustered) {
        this.clustered = clustered;
        return this;
    }

    /**
     * The VALIDATE keyword defines whether a primary key constraint on a column in a table
     * should be checked if it refers to a valid row or not.
     * @return true if ENABLE VALIDATE (this is the default), or false if ENABLE NOVALIDATE.
     */
    public boolean shouldValidate() {
        return shouldValidate;
    }

    /**
     * @param shouldValidate - if shouldValidate is set to FALSE then the constraint will be created
     * with the 'ENABLE NOVALIDATE' mode. This means the constraint would be created, but that no
     * check will be done to ensure old data has valid primary keys - only new data would be checked
     * to see if it complies with the constraint logic. The default state for primary keys is to
     * have 'ENABLE VALIDATE' set.
     */
    public AddPrimaryKeyStatement setShouldValidate(boolean shouldValidate) {
        this.shouldValidate = shouldValidate;
        return this;
    }
}
