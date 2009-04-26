package liquibase.database.statement;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class RenameTableStatement implements SqlStatement {
    private String schemaName;
    private String oldTableName;
    private String newTableName;

    public RenameTableStatement(String schemaName, String oldTableName, String newTableName) {
        this.schemaName = schemaName;
        this.oldTableName = oldTableName;
        this.newTableName = newTableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getOldTableName() {
        return oldTableName;
    }

    public String getNewTableName() {
        return newTableName;
    }
}
