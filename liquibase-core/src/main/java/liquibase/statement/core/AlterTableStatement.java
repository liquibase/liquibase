package liquibase.statement.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.statement.AbstractSqlStatement;

public class AlterTableStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;

    private List<AddColumnStatement> addColumns = new ArrayList<AddColumnStatement>();
    private List<DropColumnStatement> dropColumns = new ArrayList<DropColumnStatement>();

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

    public List<AddColumnStatement> getAddColumns() {
        return addColumns;
    }

    public void setAddColumns(List<AddColumnStatement> columns) {
        this.addColumns = columns;
    }

    public void addColumn(AddColumnStatement column) {
        addColumns.add(column);
    }

    public List<DropColumnStatement> getDropColumns() {
        return dropColumns;
    }

    public void setDropColumn(List<DropColumnStatement> columns) {
        this.dropColumns = columns;
    }

    public void dropColumn(DropColumnStatement column) {
        dropColumns.add(column);
    }
}
