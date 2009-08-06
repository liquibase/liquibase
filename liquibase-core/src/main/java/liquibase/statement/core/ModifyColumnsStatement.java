package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.SqlStatement;

public class ModifyColumnsStatement implements SqlStatement {
    private String schemaName;
    private String tableName;
    private ColumnConfig[] columns;

    public ModifyColumnsStatement(String schemaName, String tableName, ColumnConfig... columns) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public ColumnConfig[] getColumns() {
        return columns;
    }
}
