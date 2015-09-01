package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.precondition.AbstractPrecondition;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.exception.*;
import liquibase.precondition.Precondition;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

public class PrimaryKeyExistsPrecondition extends AbstractPrecondition {
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

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (getPrimaryKeyName() == null && getTableName() == null) {
            validationErrors.addError("Either primaryKeyName or tableName must be set");
        }
        return validationErrors;
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        try {
            PrimaryKey example = new PrimaryKey();
            Table table = new Table();
            table.setSchema(new Schema(getCatalogName(), getSchemaName()));
            if (StringUtils.trimToNull(getTableName()) != null) {
                table.setName(getTableName());
            }
            example.setTable(table);
            example.setName(getPrimaryKeyName());

            if (!SnapshotGeneratorFactory.getInstance().has(example, database)) {
                if (tableName != null) {
                    throw new PreconditionFailedException("Primary Key does not exist on " + database.escapeObjectName(getTableName(), Table.class), changeLog, this);
                } else {
                    throw new PreconditionFailedException("Primary Key " + database.escapeObjectName(getPrimaryKeyName(), PrimaryKey.class) + " does not exist", changeLog, this);
                }
            }
        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "primaryKeyExists";
    }
}