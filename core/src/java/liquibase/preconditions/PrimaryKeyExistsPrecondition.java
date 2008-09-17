package liquibase.preconditions;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.DatabaseChangeLog;
import liquibase.util.StringUtils;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.JDBCException;

public class PrimaryKeyExistsPrecondition implements Precondition {
    private String schemaName;
    private String primaryKeyName;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        DatabaseSnapshot databaseSnapshot;
        try {
            databaseSnapshot = database.createDatabaseSnapshot(getSchemaName(), null);
        } catch (JDBCException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
        if (databaseSnapshot.getPrimaryKey(getPrimaryKeyName()) == null) {
            throw new PreconditionFailedException("Primary Key "+database.escapeStringForDatabase(getPrimaryKeyName())+" does not exist", changeLog, this);
        }
    }

    public String getTagName() {
        return "primaryKeyExists";
    }
}