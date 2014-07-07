package liquibase.statement.core;

import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

/**
 * Adds a default value to an existing column.
 */
public class AddDefaultValueStatement extends AbstractColumnStatement {
    public static final String COLUMN_DATA_TYPE = "columnDataType";
    public static final String DEFAULT_VALUE = "defaultValue";

    public AddDefaultValueStatement() {
    }

    public AddDefaultValueStatement(String catalogName, String schemaName, String tableName, String columnName, String columnDataType) {
        this(catalogName, schemaName, tableName, columnName, columnDataType, null);
    }

    public AddDefaultValueStatement(String catalogName, String schemaName, String tableName, String columnName, String columnDataType, Object defaultValue) {
        super(catalogName, schemaName, tableName, columnName);
        setColumnDataType(columnDataType);
        setDefaultValue(defaultValue);
    }

    public String getColumnDataType() {
		return getAttribute(COLUMN_DATA_TYPE, String.class);
	}

    public AddDefaultValueStatement setColumnDataType(String dataType) {
        return (AddDefaultValueStatement) setAttribute(COLUMN_DATA_TYPE, dataType);
    }

    public Object getDefaultValue() {
        return getAttribute(DEFAULT_VALUE, Object.class);
    }

    public AddDefaultValueStatement setDefaultValue(Object defaultValue) {
        return (AddDefaultValueStatement) setAttribute(DEFAULT_VALUE, defaultValue);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
                new Column()
                        .setRelation(new Table().setName(getTableName()).setSchema(new Schema(getCatalogName(), getSchemaName())))
                        .setName(getColumnName())
        };
    }
}
