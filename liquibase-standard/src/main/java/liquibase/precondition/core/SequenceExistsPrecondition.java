package liquibase.precondition.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.*;
import liquibase.precondition.AbstractPrecondition;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SequenceExistsPrecondition extends AbstractPrecondition {
    private String catalogName;
    private String schemaName;
    private String sequenceName;

    private static final String SQL_CHECK_POSTGRES_SEQUENCE_EXISTS = "SELECT c.relname FROM pg_class c JOIN pg_namespace ns on c.relnamespace = ns.oid WHERE c.relkind = 'S' AND ns.nspname = ? and c.relname ILIKE ?";

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

        try {
            if (database instanceof PostgresDatabase) {
                checkPostgresSequence(database, changeLog);
            } else {
                Schema schema = new Schema(getCatalogName(), getSchemaName());
                if (!SnapshotGeneratorFactory.getInstance().has(new Sequence().setName(getSequenceName()).setSchema(schema), database)) {
                    throw new PreconditionFailedException("Sequence " + database.escapeSequenceName(getCatalogName(), getSchemaName(), getSequenceName()) + " does not exist", changeLog, this);
                }
            }
        } catch (LiquibaseException | SQLException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    private void checkPostgresSequence(Database database, DatabaseChangeLog changeLog) throws DatabaseException, SQLException, PreconditionFailedException, PreconditionErrorException {
        try (PreparedStatement statement = ((JdbcConnection) database.getConnection()).prepareStatement(SQL_CHECK_POSTGRES_SEQUENCE_EXISTS)) {
            statement.setString(1, getSchemaName() != null ? getSchemaName() : database.getDefaultSchemaName());
            statement.setString(2, getSequenceName());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new PreconditionFailedException("Sequence " + database.escapeSequenceName(getCatalogName(), getSchemaName(), getSequenceName()) + " does not exist", changeLog, this);
                }
            } catch (SQLException e) {
                throw new PreconditionErrorException(e, changeLog, this);
            }
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
