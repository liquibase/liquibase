package liquibase.database.sql;

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

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        }

        String sql = "DROP SEQUENCE " + database.escapeSequenceName(getSchemaName(), getSequenceName());
        if (database instanceof PostgresDatabase) {
            sql += " CASCADE";
        }

        return sql;
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return database.supportsSequences();
    }
}
