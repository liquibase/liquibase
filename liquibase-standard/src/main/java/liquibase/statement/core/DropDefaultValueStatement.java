package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropDefaultValueStatement extends AbstractSqlStatement {

    private final String catalogName;
    private final String schemaName;
    private final String tableName;
    private final String columnName;
    private final String columnDataType;

    public DropDefaultValueStatement(String catalogName, String schemaName, String tableName, String columnName, String columnDataType) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnDataType = columnDataType;
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

    public String getColumnName() {
        return columnName;
    }
    
    public String getColumnDataType() {
		return columnDataType;
	}

}
