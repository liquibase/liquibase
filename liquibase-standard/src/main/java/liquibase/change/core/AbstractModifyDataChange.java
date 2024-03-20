package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChangeProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates common fields for update and delete changes.
 */
public abstract class AbstractModifyDataChange extends AbstractChange {

    protected String catalogName;
    protected String schemaName;
    protected String tableName;

    protected List<ColumnConfig> whereParams = new ArrayList<>();

    protected String where;

    @DatabaseChangeProperty(mustEqualExisting ="table.catalog", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting= "table", description = "Name of the table")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(serializationType = SerializationType.NESTED_OBJECT, exampleValue = "name='Bob'",
        description = "SQL WHERE condition string")
    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    /**
     * @deprecated use getWhere().
     */
    @DatabaseChangeProperty(isChangeProperty = false, description = "Deprecated. Use 'where'")
    @Deprecated
    public String getWhereClause() {
        return where;
    }

    /**
     * @deprecated use setWhere()
     */
    @Deprecated
    public void setWhereClause(String where) {
        this.where = where;
    }

    public void addWhereParam(ColumnConfig param) {
        whereParams.add(param);
    }

    public void removeWhereParam(ColumnConfig param) {
        whereParams.remove(param);
    }

    @DatabaseChangeProperty(description = "Parameters for the \"where\" condition. Inserted in place of the :name and :value " +
        "placeholders in the WHERE clause. If multiple, inserted in the order they are defined.")
    public List<ColumnConfig> getWhereParams() {
        return whereParams;
    }
}
