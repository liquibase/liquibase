package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;
import liquibase.util.StringUtil;

public class AddPrimaryKeyStatement extends AbstractSqlStatement {

    private final String catalogName;
    private final String schemaName;
    private final String tableName;
    private String tablespace;
    private final ColumnConfig[] columns;
    private final String constraintName;
    private Boolean clustered;

    private String forIndexName;
    private String forIndexSchemaName;
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

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTablespace() {
        return tablespace;
    }

    public AddPrimaryKeyStatement setTablespace(String tablespace) {
        this.tablespace = tablespace;
        return this;
    }

    public ColumnConfig[] getColumns() {
        return columns;
    }

    public String getColumnNames() {
        return StringUtil.join(columns, ", ", (StringUtil.StringUtilFormatter<ColumnConfig>) obj -> obj.getName() + (obj.getDescending() != null && obj.getDescending() ? " DESC" : ""));
    }


    public String getConstraintName() {
        return constraintName;
    }

    public Boolean isClustered() {
        return clustered;
    }

    public AddPrimaryKeyStatement setClustered(Boolean clustered) {
        this.clustered = clustered;
        return this;
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
