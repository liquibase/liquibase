package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

/**
 * Renames an existing column. Includes attributes for {@link #getColumnDataType()} and {@link #getRemarks()} because some databases require data type to rename and/or lose remarks in the rename process.
 */
public class RenameColumnStatement extends AbstractColumnStatement {

    public static final String NEW_COLUMN_NAME = "newColumnName";
    public static final String COLUMN_DATA_TYPE = "columnDataType";
    public static final String REMARKS = "remarks";

    public RenameColumnStatement() {
    }

    public RenameColumnStatement(String catalogName, String schemaName, String tableName, String oldColumnName, String newColumnName, String columnDataType) {
        super(catalogName, schemaName, tableName, oldColumnName);
        setNewColumnName(newColumnName);
        setColumnDataType(columnDataType);
    }


    public String getNewColumnName() {
        return getAttribute(NEW_COLUMN_NAME, String.class);
    }

    public RenameColumnStatement setNewColumnName(String newColumnName) {
        return (RenameColumnStatement) setAttribute(NEW_COLUMN_NAME, newColumnName);
    }

    public String getColumnDataType() {
        return getAttribute(COLUMN_DATA_TYPE, String.class);
    }

    public RenameColumnStatement setColumnDataType(String columnDataType) {
        return (RenameColumnStatement) setAttribute(COLUMN_DATA_TYPE, columnDataType);
    }

    public String getRemarks() {
        return getAttribute(REMARKS, String.class);
    }

    public RenameColumnStatement setRemarks(String remarks) {
        return (RenameColumnStatement) setAttribute(REMARKS, remarks);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[]{
                new Column().setName(getColumnName()).setRelation(new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName())),
                new Column().setName(getNewColumnName()).setRelation(new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName())),
        };
    }
}

