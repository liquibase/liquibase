package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.Precondition;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.util.StringUtils;

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

    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        try {
            if (!DatabaseSnapshotGeneratorFactory.getInstance().getGenerator(database).hasIndex(getSchemaName(), getIndexName(), database)) {
                throw new PreconditionFailedException("Index "+database.escapeStringForDatabase(getIndexName())+" does not exist", changeLog, this);
            }
        } catch (DatabaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    public String getName() {
        return "indexExists";
    }
}