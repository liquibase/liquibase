package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;
import lombok.Setter;

@Getter
public class AddDefaultValueStatement extends AbstractSqlStatement {
    private final String columnName;
    private final String columnDataType;
    private final Object defaultValue;

    @Setter
    private String defaultValueConstraintName;
    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public AddDefaultValueStatement(String catalogName, String schemaName, String tableName, String columnName, String columnDataType) {
        this(catalogName, schemaName, tableName, columnName, columnDataType, null);
    }

    public AddDefaultValueStatement(String catalogName, String schemaName, String tableName, String columnName, String columnDataType, Object defaultValue) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.defaultValue = defaultValue;
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

}
