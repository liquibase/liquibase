package liquibase.change;

import lombok.Setter;

@Setter
public abstract class AbstractTableChange extends AbstractChange {
    protected String catalogName;
    protected String schemaName;
    protected String tableName;

    @DatabaseChangeProperty(since = "3.0", mustEqualExisting ="table.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(
            description="Name of the table",
            mustEqualExisting= "table", requiredForDatabase = ChangeParameterMetaData.ALL)
    public String getTableName() {
        return tableName;
    }

}
