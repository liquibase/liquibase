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
    private Boolean nullable;
    private Boolean autoIncrement;
    private Boolean primaryKey;
    
    public ModifyDataTypeChange() {
        super("modifyDataType", "Modify data type", ChangeMetaData.PRIORITY_DEFAULT);
    }

    public String getConfirmationMessage() {
        return tableName+"."+columnName+" datatype was changed to "+newDataType;
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {new ModifyDataTypeStatement(getSchemaName(), getTableName(), getColumnName(), getNewDataType(), isNullable(), getPrimaryKey(), getAutoIncrement())};
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

    public Boolean isNullable() {
        return nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    public Boolean getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Boolean getAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(Boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }
}
