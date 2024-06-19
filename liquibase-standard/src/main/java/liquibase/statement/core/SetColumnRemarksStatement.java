package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.util.ColumnParentType;

public class SetColumnRemarksStatement extends AbstractSqlStatement {

    private final String columnName;
    private final String remarks;
    private final String columnDataType;
    private String columnParentType;
    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public SetColumnRemarksStatement(String catalogName, String schemaName, String tableName, String columnName, String remarks) {
        this(catalogName, schemaName, tableName, columnName, remarks, null);
    }

    public SetColumnRemarksStatement(String catalogName,
                                     String schemaName,
                                     String tableName,
                                     String columnName,
                                     String remarks,
                                     String columnDataType) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.columnName = columnName;
        this.remarks = remarks;
        this.columnDataType = columnDataType;
    }

    public SetColumnRemarksStatement(String catalogName,
                                     String schemaName,
                                     String tableName,
                                     String columnName,
                                     String remarks,
                                     String columnDataType,
                                     String columnParentType) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.columnName = columnName;
        this.remarks = remarks;
        this.columnDataType = columnDataType;
        this.columnParentType = columnParentType;
    }

    public String getColumnDataType() {
        return columnDataType;
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

    public String getRemarks() {
        return remarks;
    }

    public ColumnParentType getColumnParentType() {
        return columnParentType != null ? ColumnParentType.valueOf(columnParentType) : null;
    }
}
