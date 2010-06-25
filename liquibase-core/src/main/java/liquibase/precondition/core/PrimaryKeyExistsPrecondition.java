package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.precondition.Precondition;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.util.StringUtils;

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

    public Warnings warn(Database database) {
        return new Warnings();
    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        DatabaseSnapshot snapshot;
        try {
            snapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, getSchemaName(), null);
        } catch (DatabaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
        if (tableName != null) {
            if (snapshot.getPrimaryKeyForTable(getTableName()) == null) {
                throw new PreconditionFailedException("Primary Key does not exist on "+database.escapeStringForDatabase(getTableName()), changeLog, this);
            }
        } else if (primaryKeyName != null) {
            if (snapshot.getPrimaryKey(getPrimaryKeyName()) == null) {
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