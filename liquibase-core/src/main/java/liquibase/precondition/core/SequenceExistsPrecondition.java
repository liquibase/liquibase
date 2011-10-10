package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.precondition.Precondition;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
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

        public Warnings warn(Database database) {
        return new Warnings();
    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        DatabaseSnapshot snapshot;
        String currentSchemaName;
        try {
            currentSchemaName = getSchemaName() == null ? (database == null ? null: database.getDefaultSchemaName()) : getSchemaName();
            snapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, currentSchemaName, null);
        } catch (DatabaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
        if (snapshot.getSequence(getSequenceName()) == null) {
            throw new PreconditionFailedException("Sequence "+database.escapeSequenceName(currentSchemaName, getSequenceName())+" does not exist", changeLog, this);
        }
    }

    public String getName() {
        return "sequenceExists";
    }
}