package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class RenameColumnStatement extends AbstractSqlStatement {

    private String oldColumnName;
    private String newColumnName;
    private String columnDataType;
    private String remarks;
    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public RenameColumnStatement(String catalogName, String schemaName, String tableName, String oldColumnName, String newColumnName, String columnDataType) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.oldColumnName = oldColumnName;
        this.newColumnName = newColumnName;
        this.columnDataType = columnDataType;
    }


    public RenameColumnStatement(String catalogName, String schemaName, String tableName, String oldColumnName, String newColumnName, String columnDataType,String remarks) {
        this(catalogName, schemaName, tableName, oldColumnName, newColumnName, columnDataType);
        this.remarks = remarks;
    }

    public String getCatalogName() {
        return databaseTableIdentifier.getCatalogName();
    }

    public String getSchemaName() {
        return databaseTableIdentifier.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        this.databaseTableIdentifier.setSchemaName(schemaName);
    }

    public String getTableName() {
        return databaseTableIdentifier.getTableName();
    }

    public void setTableName(String tableName) {
        this.databaseTableIdentifier.setTableName(tableName);
    }

    public String getOldColumnName() {
        return oldColumnName;
    }

    public void setOldColumnName(String oldColumnName) {
        this.oldColumnName = oldColumnName;
    }

    public String getNewColumnName() {
        return newColumnName;
    }

    public void setNewColumnName(String newColumnName) {
        this.newColumnName = newColumnName;
    }

    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}

