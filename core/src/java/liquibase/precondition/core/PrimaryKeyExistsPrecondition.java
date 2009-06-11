package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.exception.JDBCException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.util.StringUtils;
import liquibase.precondition.Precondition;

public class PrimaryKeyExistsPrecondition implements Precondition {
    private String schemaName;
    private String primaryKeyName;
    private String tableName;

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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        DatabaseSnapshot databaseSnapshot;
        try {
            databaseSnapshot = database.createDatabaseSnapshot(getSchemaName(), null);
        } catch (JDBCException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
        if (tableName != null) {
            if (databaseSnapshot.getPrimaryKeyForTable(getTableName()) == null) {
                throw new PreconditionFailedException("Primary Key does not exist on "+database.escapeStringForDatabase(getTableName()), changeLog, this);
            }
        } else if (primaryKeyName != null) {
            if (databaseSnapshot.getPrimaryKey(getPrimaryKeyName()) == null) {
                throw new PreconditionFailedException("Primary Key "+database.escapeStringForDatabase(getPrimaryKeyName())+" does not exist", changeLog, this);
            }
        } else {
            throw new RuntimeException("primaryKeyExists precondition requires a tableName or primaryKeyName");
        }
    }

    public String getName() {
        return "primaryKeyExists";
    }
}