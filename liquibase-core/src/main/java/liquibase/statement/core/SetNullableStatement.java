package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

/**
 * Mark a column as nullable or not. Some database types require columnDataType to be set.
 */
public class SetNullableStatement extends AbstractColumnStatement {

    public static final String COLUMN_DATA_TYPE = "columnDataType";
    public static final String NULLABLE = "nullable";

    public SetNullableStatement() {
    }

    public SetNullableStatement(String catalogName, String schemaName, String tableName, String columnName, String columnDataType, boolean nullable) {
        super(catalogName, schemaName, tableName, columnName);
        setColumnDataType(columnDataType);
        setNullable(nullable);
    }

    public String getColumnDataType() {
        return getAttribute(COLUMN_DATA_TYPE, String.class);
    }

    public SetNullableStatement setColumnDataType(String dataType) {
        return (SetNullableStatement) setAttribute(COLUMN_DATA_TYPE, dataType);
    }

    /**
     * Return if the column should be nullable. Defaults to false.
     */
    public boolean isNullable() {
        return getAttribute(NULLABLE, false);
    }

    public SetNullableStatement setNullable(boolean nullable) {
        return (SetNullableStatement) setAttribute(NULLABLE, nullable);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Column().setName(getColumnName()).setRelation(new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName()))
        };
    }
}
