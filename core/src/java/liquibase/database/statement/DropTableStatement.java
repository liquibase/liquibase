package liquibase.database.statement;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.log.LogFactory;

public class DropTableStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private boolean cascadeConstraints;

    public DropTableStatement(String schemaName, String tableName, boolean cascadeConstraints) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.cascadeConstraints = cascadeConstraints;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isCascadeConstraints() {
        return cascadeConstraints;
    }
}
