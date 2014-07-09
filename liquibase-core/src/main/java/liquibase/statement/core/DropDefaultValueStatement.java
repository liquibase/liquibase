package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

/**
 * Drops the default value of an existing column.
 */
public class DropDefaultValueStatement extends AbstractColumnStatement {

    public static final String COLUMN_DATA_TYPE = "columnDataType";

    public DropDefaultValueStatement() {
    }

    public DropDefaultValueStatement(String catalogName, String schemaName, String tableName, String columnName, String columnDataType) {
        super(catalogName, schemaName, tableName, columnName);
        setColumnDataType(columnDataType);
    }
    public String getColumnDataType() {
		return getAttribute(COLUMN_DATA_TYPE, String.class);
	}

    public DropDefaultValueStatement setColumnDataType(String dataType) {
        return (DropDefaultValueStatement) setAttribute(COLUMN_DATA_TYPE, dataType);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Column().setName(getColumnName()).setRelation(new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName()))
        };
    }
}
