package liquibase.precondition;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.exception.JDBCException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.util.StringUtils;

public class SequenceExistsPrecondition implements Precondition {
    private String schemaName;
    private String sequenceName;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        DatabaseSnapshot databaseSnapshot;
        try {
            databaseSnapshot = database.createDatabaseSnapshot(getSchemaName(), null);
        } catch (JDBCException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
        if (databaseSnapshot.getSequence(getSequenceName()) == null) {
            throw new PreconditionFailedException("Sequence "+database.escapeSequenceName(getSchemaName(), getSequenceName())+" does not exist", changeLog, this);
        }
    }

    public String getTagName() {
        return "sequenceExists";
    }
}