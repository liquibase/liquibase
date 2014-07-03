package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.core.DB2Database;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.Statement;
import liquibase.statement.core.ModifyDataTypeStatement;
import liquibase.statement.core.ReindexStatement;

@DatabaseChange(name="modifyDataType", description = "Modify data type", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class ModifyDataTypeChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String newDataType;

    @Override
    public String getConfirmationMessage() {
        return tableName+"."+columnName+" datatype was changed to "+newDataType;
    }

    @Override
    public Statement[] generateStatements(ExecutionEnvironment env) {
        ModifyDataTypeStatement modifyDataTypeStatement = new ModifyDataTypeStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getNewDataType());
        if (env.getTargetDatabase() instanceof DB2Database) {
            return new Statement[] {
                    modifyDataTypeStatement,
                    new ReindexStatement(getCatalogName(), getSchemaName(), getTableName())
            };
        } else {
            return new Statement[] {
                    modifyDataTypeStatement
            };
        }
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column")
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @DatabaseChangeProperty()
    public String getNewDataType() {
        return newDataType;
    }

    public void setNewDataType(String newDataType) {
        this.newDataType = newDataType;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
