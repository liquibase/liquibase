package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

import java.util.LinkedHashMap;
import java.util.Map;

public class InsertStatement extends AbstractStatement {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private Map<String, Object> columnValues = new LinkedHashMap<String, Object>();
    private boolean needsPreparedStatement;

    public InsertStatement(String catalogName, String schemaName, String tableName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public String getCatalogName() {
        return catalogName;
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
    
    public InsertStatement addColumn(ColumnConfig columnConfig) {
    	return addColumnValue(columnConfig.getName(), columnConfig.getValueObject());
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName())
        };
    }

    public void setNeedsPreparedStatement(boolean needsPreparedStatement) {
        this.needsPreparedStatement = needsPreparedStatement;
    }

    public boolean getNeedsPreparedStatement() {
        return needsPreparedStatement;
    }
}
