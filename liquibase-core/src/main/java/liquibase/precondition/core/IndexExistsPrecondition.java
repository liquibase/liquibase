package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.precondition.Precondition;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.util.StringUtils;

public class IndexExistsPrecondition implements Precondition {
    private String schemaName;
    private String tableName;
    private String columnNames;
    private String indexName;

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

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public Warnings warn(Database database) {
        return new Warnings();
    }

    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (getIndexName() == null && getTableName() == null && getColumnNames() == null) {
            validationErrors.addError("indexName OR tableName and columnNames is required");
        }
        return validationErrors;
    }

    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
    	String currentSchemaName;
    	try {
            currentSchemaName = getSchemaName() == null ? (database == null ? null: database.getDefaultSchemaName()) : getSchemaName();
            if (!DatabaseSnapshotGeneratorFactory.getInstance().getGenerator(database).hasIndex(currentSchemaName, getTableName(), getIndexName(), database, getColumnNames())) {
                String name = "";

                if (getIndexName() != null) {
                    name += database.escapeStringForDatabase(getIndexName());
                }

                if (StringUtils.trimToNull(getTableName()) != null) {
                    name += " on "+database.escapeStringForDatabase(getTableName());

                    if (StringUtils.trimToNull(getColumnNames()) != null) {
                        name += " columns "+getColumnNames();
                    }
                }
                throw new PreconditionFailedException("Index "+ name +" does not exist", changeLog, this);
            }
        } catch (DatabaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    public String getName() {
        return "indexExists";
    }
}