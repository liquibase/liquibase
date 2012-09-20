package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.snapshot.jvm.DatabaseObjectGeneratorFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.exception.*;
import liquibase.precondition.Precondition;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

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
        Column example = new Column();
        if (StringUtils.trimToNull(getTableName()) != null) {
            example.setRelation(new Table().setName(getTableName()));
        }
        example.setName(getColumnName());

        try {
            if (!DatabaseObjectGeneratorFactory.getInstance().getGenerator(Column.class, database).has(database.correctSchema(new Schema(getCatalogName(), getSchemaName())), example, database)) {
                throw new PreconditionFailedException("Column '" + database.escapeColumnName(catalogName, schemaName, getTableName(), getColumnName()) + "' does not exist", changeLog, this);
            }
        } catch (DatabaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    public String getName() {
        return "columnExists";
    }
}
