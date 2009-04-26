package liquibase.database.statement;

import liquibase.database.Database;

import java.util.HashMap;
import java.util.Map;

public class InsertStatement implements SqlStatement {
    private String schemaName;
    private String tableName;
    private Map<String, Object> columnValues = new HashMap<String, Object>();

    public InsertStatement(String schemaName, String tableName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
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
}
