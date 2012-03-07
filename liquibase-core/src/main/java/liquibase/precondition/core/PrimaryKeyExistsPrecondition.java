package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.structure.Schema;
import liquibase.exception.*;
import liquibase.precondition.Precondition;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;

public class PrimaryKeyExistsPrecondition implements Precondition {
    private String catalogName;
    private String schemaName;
    private String primaryKeyName;
    private String tableName;

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
        try {
            if (!DatabaseSnapshotGeneratorFactory.getInstance().getGenerator(database).hasPrimaryKey(new Schema(getCatalogName(), getSchemaName()), getTableName(), getPrimaryKeyName(), database)) {
                if (tableName != null) {
                    throw new PreconditionFailedException("Primary Key does not exist on "+database.escapeStringForDatabase(getTableName()), changeLog, this);
                } else {
                    throw new PreconditionFailedException("Primary Key "+database.escapeStringForDatabase(getPrimaryKeyName())+" does not exist", changeLog, this);
                }
            }
        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    public String getName() {
        return "primaryKeyExists";
    }
}