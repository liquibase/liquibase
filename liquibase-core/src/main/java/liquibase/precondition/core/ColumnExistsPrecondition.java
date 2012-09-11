package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.structure.core.Schema;
import liquibase.exception.*;
import liquibase.precondition.Precondition;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;

public class ColumnExistsPrecondition implements Precondition {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;

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

    public Warnings warn(Database database) {
        return new Warnings();
    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        if (!DatabaseSnapshotGeneratorFactory.getInstance().getGenerator(database).hasColumn(database.correctSchema(new Schema(getCatalogName(), getSchemaName())), getTableName(), getColumnName(), database)) {
            throw new PreconditionFailedException("Column '" + database.escapeColumnName(catalogName, schemaName, getTableName(), getColumnName()) + "' does not exist", changeLog, this);
        }
    }

    public String getName() {
        return "columnExists";
    }
}
