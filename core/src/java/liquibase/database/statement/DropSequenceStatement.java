package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class DropSequenceStatement implements SqlStatement {

    private String schemaName;
    private String sequenceName;

    public DropSequenceStatement(String schemaName, String sequenceName) {
        this.schemaName = schemaName;
        this.sequenceName = sequenceName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getSequenceName() {
        return sequenceName;
    }
}
