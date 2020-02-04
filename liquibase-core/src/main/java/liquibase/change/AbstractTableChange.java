package liquibase.change;

/** Abstract parent for table related changes having catalogName, schemaName and mandatory string properties */
public abstract class AbstractTableChange extends AbstractChange {
    public enum Params{
        catalogName,schemaName,tableName,column;
    }
    protected String catalogName;
    protected String schemaName;
    protected String tableName;
    @DatabaseChangeProperty(since = "3.0", mustEqualExisting ="table.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(
            description="Name of the table",
            mustEqualExisting= "table", requiredForDatabase = ChangeParameterMetaData.ALL)
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}