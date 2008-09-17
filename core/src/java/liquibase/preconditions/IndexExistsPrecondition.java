package liquibase.preconditions;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.DatabaseChangeLog;
import liquibase.util.StringUtils;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.JDBCException;

public class IndexExistsPrecondition implements Precondition {
    private String schemaName;
    private String indexName;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        DatabaseSnapshot databaseSnapshot;
        try {
            databaseSnapshot = database.createDatabaseSnapshot(getSchemaName(), null);
        } catch (JDBCException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
        if (databaseSnapshot.getIndex(getIndexName()) == null) {
            throw new PreconditionFailedException("Index "+database.escapeStringForDatabase(getIndexName())+" does not exist", changeLog, this);
        }
    }

    public String getTagName() {
        return "indexExists";
    }
}