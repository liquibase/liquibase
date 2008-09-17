package liquibase.preconditions;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.DatabaseChangeLog;
import liquibase.util.StringUtils;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.JDBCException;

public class TableExistsPrecondition implements Precondition {
    private String schemaName;
    private String tableName;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        DatabaseSnapshot databaseSnapshot = null;
        try {
            databaseSnapshot = database.createDatabaseSnapshot(getSchemaName(), null);
        } catch (JDBCException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
        if (databaseSnapshot.getTable(getTableName()) == null) {
            throw new PreconditionFailedException("Table "+database.escapeTableName(getSchemaName(), getTableName())+" does not exist", changeLog, this);
        }
    }

    public String getTagName() {
        return "tableExists";
    }
}
