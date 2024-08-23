package liquibase.precondition.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.*;
import liquibase.precondition.AbstractPrecondition;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtil;
import liquibase.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.String.format;

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
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
		if (canCheckFast(database)) {
			checkFast(database, changeLog);

        } else {
            checkUsingSnapshot(database, changeLog);
        }
    }

    private void checkUsingSnapshot(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        Column example = new Column();
        if (StringUtil.trimToNull(getTableName()) != null) {
            String schemaName = getSchemaName();
            if (schemaName == null) {
                schemaName = database.getDefaultSchemaName();
            }
            example.setRelation(new Table().setName(database.correctObjectName(getTableName(), Table.class)).setSchema(new Schema(getCatalogName(), schemaName)));
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

    private boolean canCheckFast(Database database) {
        if (getCatalogName() != null)
            return false;

        if (!(database.getConnection() instanceof JdbcConnection))
            return false;

        if (getColumnName() == null)
            return false;

        if (!getColumnName().matches("(?i)[a-z][a-z_0-9]*"))
            return false;

        return (getSchemaName() != null) || (database.getDefaultSchemaName() != null);
    }

    private void checkFast(Database database, DatabaseChangeLog changeLog)
            throws PreconditionFailedException, PreconditionErrorException {

        try {
            String schemaName = getSchemaName();
            if (schemaName == null) {
                schemaName = database.getDefaultSchemaName();
            }
            String tableName = getTableName();
            String columnName = getColumnName();

            if (database instanceof PostgresDatabase) {
                makeSureColumnExistsInPostgres(database, changeLog, schemaName, tableName, columnName);
            } else {
                makeSureColumnExistsInOtherDBs(database, changeLog, schemaName, tableName, columnName);
            }

        } catch (DatabaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    private void makeSureColumnExistsInOtherDBs(Database database, DatabaseChangeLog changeLog, String schemaName, String tableName, String columnName) throws PreconditionFailedException {
        String sql;
        if (database instanceof FirebirdDatabase) {
            sql = format("select t.%s from %s t where 0=1",
                    database.escapeColumnNameList(columnName),
                    database.escapeObjectName(tableName, Table.class));
        } else {
            sql = format("select t.%s from %s.%s t where 0=1",
                    database.escapeColumnNameList(columnName),
                    database.escapeObjectName(schemaName, Schema.class),
                    database.escapeObjectName(tableName, Table.class));
        }

        try (PreparedStatement statement2 = ((JdbcConnection) database.getConnection()).prepareStatement(sql);
             ResultSet rs = statement2.executeQuery()
        ){
            // column exists
        } catch (SQLException | DatabaseException e) {
            // column or table does not exist
            throw new PreconditionFailedException(format(
                    "Column %s.%s.%s does not exist", schemaName,
                    tableName, columnName), changeLog, this);
        }
    }

    private void makeSureColumnExistsInPostgres(Database database, DatabaseChangeLog changeLog, String schemaName, String tableName, String columnName) throws PreconditionFailedException, PreconditionErrorException, DatabaseException {
        String sql = "SELECT 1 FROM pg_attribute a WHERE EXISTS (SELECT 1 FROM pg_class JOIN pg_catalog.pg_namespace ns ON ns.oid = pg_class.relnamespace WHERE lower(ns.nspname) = ? AND lower(relname) = lower(?) AND pg_class.oid = a.attrelid) AND lower(a.attname) = lower(?);";
        try (PreparedStatement statement = ((JdbcConnection) database.getConnection())
                    .prepareStatement(sql)){
            statement.setString(1, schemaName.toLowerCase());
            statement.setString(2, tableName);
            statement.setString(3, columnName);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return;
                } else {
                    // column or table does not exist
                    throw new PreconditionFailedException(format("Column %s.%s.%s does not exist", schemaName, tableName, columnName), changeLog, this);
                }
            }
        } catch (SQLException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public String getName() {
        return "columnExists";
    }
}
