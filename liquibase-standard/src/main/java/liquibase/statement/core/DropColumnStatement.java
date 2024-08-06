package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class DropColumnStatement extends AbstractSqlStatement {

    @Getter
    private String columnName;

    @Getter
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
