package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DeleteStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates common fields for update and delete changes.
 */
public abstract class AbstractModifyDataChange extends AbstractChange {

    protected String catalogName;
    protected String schemaName;
    protected String tableName;

    protected List<ColumnConfig> whereParams = new ArrayList<ColumnConfig>();

    protected String whereClause;

    @DatabaseChangeProperty(mustEqualExisting ="table.catalog")
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

    @DatabaseChangeProperty(requiredForDatabase = "all", mustEqualExisting = "table")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(serializationType = SerializationType.NESTED_OBJECT)
    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public void addWhereParam(ColumnConfig param) {
        whereParams.add(param);
    }

    public void removeWhereParam(ColumnConfig param) {
        whereParams.remove(param);
    }

    public List<ColumnConfig> getWhereParams() {
        return whereParams;
    }
}
