package liquibase.precondition.core;

import liquibase.CatalogAndSchema;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.snapshot.jvm.DatabaseObjectGeneratorFactory;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Schema;
import liquibase.exception.*;
import liquibase.precondition.Precondition;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

public class ForeignKeyExistsPrecondition implements Precondition {
    private String catalogName;
    private String schemaName;
    private String foreignKeyTableName;
    private String foreignKeyName;

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

    public Warnings warn(Database database) {
        return new Warnings();
    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        try {
            ForeignKey example = new ForeignKey();
            example.setName(getForeignKeyName());
            if (StringUtils.trimToNull(getForeignKeyTableName()) != null) {
                example.setForeignKeyTable(new Table().setName(getForeignKeyTableName()));
            }
            //todo: include catalog/schema

            if (!DatabaseObjectGeneratorFactory.getInstance().getGenerator(ForeignKey.class, database).has(example, database)) {
                    throw new PreconditionFailedException("Foreign Key "+database.escapeIndexName(catalogName, schemaName, foreignKeyName)+" does not exist", changeLog, this);
            }
        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    public String getName() {
        return "foreignKeyConstraintExists";
    }
}