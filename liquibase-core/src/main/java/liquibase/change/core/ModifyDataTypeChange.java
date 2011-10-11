package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.ModifyDataTypeStatement;
import liquibase.database.Database;

@ChangeClass(name="modifyDataType", description = "Modify data type", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class ModifyDataTypeChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String columnName;
    private String newDataType;

    public String getConfirmationMessage() {
        return tableName+"."+columnName+" datatype was changed to "+newDataType;
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {new ModifyDataTypeStatement(getSchemaName(), getTableName(), getColumnName(), getNewDataType())};
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @ChangeProperty(requiredForDatabase = "all")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @ChangeProperty(requiredForDatabase = "all")
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @ChangeProperty(requiredForDatabase = "all")
    public String getNewDataType() {
        return newDataType;
    }

    public void setNewDataType(String newDataType) {
        this.newDataType = newDataType;
    }
}
