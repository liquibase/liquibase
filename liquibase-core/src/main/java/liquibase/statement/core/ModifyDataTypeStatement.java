package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

/**
 * Change the data type of an existing column
 */
public class ModifyDataTypeStatement extends AbstractColumnStatement {
    public static final String NEW_DATA_TYPE = "newDataType";

    public ModifyDataTypeStatement() {
    }

    public ModifyDataTypeStatement(String catalogName, String schemaName, String tableName, String columnName, String newDataType) {
        super(catalogName, schemaName, tableName, columnName);
        setNewDataType(newDataType);
    }

    public String getNewDataType() {
        return getAttribute(NEW_DATA_TYPE, String.class);
    }

    public ModifyDataTypeStatement setNewDataType(String newDataType) {
        return (ModifyDataTypeStatement) setAttribute(NEW_DATA_TYPE, newDataType);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}
