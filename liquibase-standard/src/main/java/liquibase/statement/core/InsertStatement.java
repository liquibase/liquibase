package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;

import java.util.LinkedHashMap;
import java.util.Map;

public class InsertStatement extends AbstractSqlStatement {
    private final Map<String, Object> columnValues = new LinkedHashMap<>();
    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public InsertStatement(String catalogName, String schemaName, String tableName) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
    }

    public String getCatalogName() {
        return databaseTableIdentifier.getCatalogName();
    }

    public String getSchemaName() {
        return databaseTableIdentifier.getSchemaName();
    }

    public String getTableName() {
        return databaseTableIdentifier.getTableName();
    }

    public InsertStatement addColumnValue(String columnName, Object newValue) {
        columnValues.put(columnName, newValue);

        return this;
    }

    public Object getColumnValue(String columnName) {
        return columnValues.get(columnName);
    }

    public Map<String, Object> getColumnValues() {
        return columnValues;
    }
    
    public InsertStatement addColumn(ColumnConfig columnConfig) {
    	return addColumnValue(columnConfig.getName(), columnConfig.getValueObject());
    }
}
