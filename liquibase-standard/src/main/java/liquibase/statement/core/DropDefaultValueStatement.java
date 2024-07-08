package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropDefaultValueStatement extends AbstractSqlStatement {

    private final String columnName;
    private final String columnDataType;
    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public DropDefaultValueStatement(String catalogName, String schemaName, String tableName, String columnName, String columnDataType) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.columnName = columnName;
        this.columnDataType = columnDataType;
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

    public String getColumnName() {
        return columnName;
    }
    
    public String getColumnDataType() {
		return columnDataType;
	}

}
