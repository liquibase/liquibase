package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

import java.util.ArrayList;
import java.util.List;

public class DropColumnStatement extends AbstractSqlStatement {

    private String columnName;

    private final List<DropColumnStatement> columns = new ArrayList<>();
    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public DropColumnStatement(String catalogName, String schemaName, String tableName, String columnName) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.columnName = columnName;
    }

    public DropColumnStatement(List<DropColumnStatement> columns) {
        this.columns.addAll(columns);
    }

    public boolean isMultiple() {
        return !columns.isEmpty();
    }

    public List<DropColumnStatement> getColumns() {
        return columns;
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

}
