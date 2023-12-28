package liquibase.precondition.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.*;
import liquibase.precondition.AbstractPrecondition;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrimaryKeyExistsPrecondition extends AbstractPrecondition {

    private String catalogName;
    private String schemaName;
    private String primaryKeyName;
    private String tableName;

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if ((getPrimaryKeyName() == null) && (getTableName() == null)) {
            validationErrors.addError("Either primaryKeyName or tableName must be set");
        }
        return validationErrors;
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        try {
            PrimaryKey example = new PrimaryKey();
            Table table = new Table();
            table.setSchema(new Schema(getCatalogName(), getSchemaName()));
            if (StringUtil.trimToNull(getTableName()) != null) {
                table.setName(getTableName());
            } else if (database instanceof H2Database || database instanceof MySQLDatabase || database instanceof HsqlDatabase
                || database instanceof SQLiteDatabase || database instanceof DB2Database) {
                throw new DatabaseException("Database driver requires a table name to be specified in order to search for a primary key.");
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
