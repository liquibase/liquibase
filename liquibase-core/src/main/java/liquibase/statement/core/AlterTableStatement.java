package liquibase.statement.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.statement.AbstractSqlStatement;

public class AlterTableStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;

    private List<AddColumnStatement> columns = new ArrayList<AddColumnStatement>();

    public AlterTableStatement(String catalogName, String schemaName, String tableName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
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

    public List<AddColumnStatement> getColumns() {
        return columns;
    }

    public void setColumns(List<AddColumnStatement> columns) {
        this.columns = columns;
    }

    public void addColumn(AddColumnStatement column) {
        columns.add(column);
    }
}
