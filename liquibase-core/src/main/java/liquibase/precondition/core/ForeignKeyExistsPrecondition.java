package liquibase.precondition.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.AbstractPrecondition;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

public class ForeignKeyExistsPrecondition extends AbstractPrecondition {
    private String catalogName;
    private String schemaName;
    private String foreignKeyTableName;
    private String foreignKeyName;

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

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

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener) throws PreconditionFailedException, PreconditionErrorException {
        try {
            ForeignKey example = new ForeignKey();
            example.setName(getForeignKeyName());
            example.setForeignKeyTable(new Table());
            if (StringUtil.trimToNull(getForeignKeyTableName()) != null) {
                example.getForeignKeyTable().setName(getForeignKeyTableName());
            }
            example.getForeignKeyTable().setSchema(new Schema(getCatalogName(), getSchemaName()));

            if (!SnapshotGeneratorFactory.getInstance().has(example, database)) {
                throw new PreconditionFailedException("Foreign Key " +
                    database.escapeIndexName(catalogName, schemaName, foreignKeyName) + " does not exist",
                    changeLog,
                    this
                );
            }
        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public String getName() {
        return "foreignKeyConstraintExists";
    }
}
