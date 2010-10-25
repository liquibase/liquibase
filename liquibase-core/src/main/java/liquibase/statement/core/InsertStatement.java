package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class InsertStatement extends AbstractSqlStatement {
    private String schemaName;
    private String tableName;
    private SortedMap<String, Object> columnValues = new TreeMap<String, Object>();

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
