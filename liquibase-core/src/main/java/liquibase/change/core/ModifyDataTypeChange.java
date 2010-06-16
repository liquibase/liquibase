package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.ModifyDataTypeStatement;
import liquibase.database.Database;

public class ModifyDataTypeChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String columnName;
    private String newDataType;

    public ModifyDataTypeChange() {
        super("modifyDataType", "Modify data type", ChangeMetaData.PRIORITY_DEFAULT);
    }

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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getNewDataType() {
        return newDataType;
    }

    public void setNewDataType(String newDataType) {
        this.newDataType = newDataType;
    }
}
