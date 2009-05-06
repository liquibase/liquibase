package liquibase.statement;

public class AddDefaultValueStatement implements SqlStatement {
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private Object defaultValue;


    public AddDefaultValueStatement(String schemaName, String tableName, String columnName, String columnDataType) {
        this(schemaName, tableName, columnName, columnDataType, null);
    }

    public AddDefaultValueStatement(String schemaName, String tableName, String columnName, String columnDataType, Object defaultValue) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.defaultValue = defaultValue;
    }

    public String getColumnName() {
        return columnName;
    }
    
    public String getColumnDataType() {
		return columnDataType;
	}

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
