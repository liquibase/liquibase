package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;
import lombok.Setter;

public class DropPrimaryKeyStatement extends AbstractSqlStatement {

    @Getter
    private final String constraintName;
    @Setter
    @Getter
    private Boolean dropIndex;
    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public DropPrimaryKeyStatement(String catalogName, String schemaName, String tableName, String constraintName) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.constraintName = constraintName;
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
