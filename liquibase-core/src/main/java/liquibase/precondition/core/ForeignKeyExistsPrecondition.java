package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.precondition.Precondition;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.util.StringUtils;

public class ForeignKeyExistsPrecondition implements Precondition {
    private String schemaName;
    private String foreignKeyTableName;
    private String foreignKeyName;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getForeignKeyTableName() {
        return foreignKeyTableName;
    }

    public void setForeignKeyTableName(String foreignKeyTableName) {
        this.foreignKeyTableName = foreignKeyTableName;
    }

    public String getForeignKeyName() {
        return foreignKeyName;
    }

    public void setForeignKeyName(String foreignKeyName) {
        this.foreignKeyName = foreignKeyName;
    }

    public Warnings warn(Database database) {
        return new Warnings();
    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        String currentSchemaName;
    	try {
            currentSchemaName = getSchemaName() == null ? (database == null ? null: database.getDefaultSchemaName()) : getSchemaName();
        	boolean checkPassed;
            if (getForeignKeyTableName() == null) {
                checkPassed = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, currentSchemaName, null).getForeignKey(getForeignKeyName()) != null;
            } else { //much faster if we can limit to correct table
                 checkPassed = DatabaseSnapshotGeneratorFactory.getInstance().getGenerator(database).getForeignKeyByForeignKeyTable(currentSchemaName, getForeignKeyTableName(), getForeignKeyName(), database) != null;
            }
            if (!checkPassed) {
                String message = "Foreign Key " + database.escapeStringForDatabase(getForeignKeyName());
                if (getForeignKeyTableName() != null) {
                    message += " on table "+ getForeignKeyTableName();
                }
                message +=  " does not exist";
                throw new PreconditionFailedException(message, changeLog, this);
            }
        } catch (DatabaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    public String getName() {
        return "foreignKeyConstraintExists";
    }
}