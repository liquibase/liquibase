package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.util.ColumnParentType;
import lombok.Getter;

public class SetColumnRemarksStatement extends AbstractSqlStatement {

    @Getter
    private final String columnName;
    @Getter
    private final String remarks;
    @Getter
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

    public String getCatalogName() {
        return databaseTableIdentifier.getCatalogName();
    }

    public String getSchemaName() {
        return databaseTableIdentifier.getSchemaName();
    }

    public String getTableName() {
        return databaseTableIdentifier.getTableName();
    }

    public ColumnParentType getColumnParentType() {
        return columnParentType != null ? ColumnParentType.valueOf(columnParentType) : null;
    }
}
