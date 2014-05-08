package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.precondition.AbstractPrecondition;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.exception.*;
import liquibase.precondition.Precondition;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

public class ColumnExistsPrecondition extends AbstractPrecondition {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;

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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
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
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        Column example = new Column();
        if (StringUtils.trimToNull(getTableName()) != null) {
            example.setRelation(new Table().setName(database.correctObjectName(getTableName(), Table.class)).setSchema(new Schema(getCatalogName(), getSchemaName())));
        }
        example.setName(database.correctObjectName(getColumnName(), Column.class));

        try {
            if (!SnapshotGeneratorFactory.getInstance().has(example, database)) {
                throw new PreconditionFailedException("Column '" + database.escapeColumnName(catalogName, schemaName, getTableName(), getColumnName()) + "' does not exist", changeLog, this);
            }
        } catch (LiquibaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public String getName() {
        return "columnExists";
    }
}
