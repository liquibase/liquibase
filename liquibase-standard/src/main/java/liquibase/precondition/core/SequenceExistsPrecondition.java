package liquibase.precondition.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.precondition.AbstractPrecondition;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;

public class SequenceExistsPrecondition extends AbstractPrecondition {
    private String catalogName;
    private String schemaName;
    private String sequenceName;

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

        @Override
        public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        DatabaseSnapshot snapshot;
        Schema schema = new Schema(getCatalogName(), getSchemaName());
        try {
            if (!SnapshotGeneratorFactory.getInstance().has(new Sequence().setName(getSequenceName()).setSchema(schema), database)) {
                throw new PreconditionFailedException("Sequence "+database.escapeSequenceName(getCatalogName(), getSchemaName(), getSequenceName())+" does not exist", changeLog, this);
            }
        } catch (LiquibaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "sequenceExists";
    }
}