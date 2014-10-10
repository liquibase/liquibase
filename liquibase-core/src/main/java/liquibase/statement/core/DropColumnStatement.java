package liquibase.statement.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.statement.AbstractSqlStatement;

public class DropColumnStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;

    private List<DropColumnStatement> columns = new ArrayList<DropColumnStatement>();

    public DropColumnStatement(String catalogName, String schemaName, String tableName, String columnName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
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

}
