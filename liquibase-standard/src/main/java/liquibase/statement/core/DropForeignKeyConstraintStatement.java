package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

@Getter
public class DropForeignKeyConstraintStatement extends AbstractSqlStatement {

    private final String baseTableCatalogName;
    private final String baseTableSchemaName;
    private final String baseTableName;
    private final String constraintName;

    public DropForeignKeyConstraintStatement(String baseTableCatalogName, String baseTableSchemaName, String baseTableName, String constraintName) {
        this.baseTableCatalogName = baseTableCatalogName;
        this.baseTableSchemaName = baseTableSchemaName;
        this.baseTableName = baseTableName;
        this.constraintName = constraintName;
    }

}
