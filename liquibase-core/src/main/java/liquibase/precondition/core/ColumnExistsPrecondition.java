package liquibase.precondition.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.*;
import liquibase.precondition.AbstractPrecondition;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtil;

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
            checkUsingSnapshot(database, changeLog, changeSet);
        }
    }

    private void checkUsingSnapshot(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        Column example = new Column();
        if (StringUtil.trimToNull(getTableName()) != null) {
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

    private boolean canCheckFast(Database database) {
        if (getCatalogName() != null)
            return false;

        if (!(database.getConnection() instanceof JdbcConnection))
            return false;

        if (getColumnName() == null)
            return false;

        if (!getColumnName().matches("(?i)[a-z][a-z_0-9]*"))
            return false;

        if (!((getSchemaName() != null) || (database.getDefaultSchemaName() != null))) {
            return false;
        }

        return true;
    }

    private void checkFast(Database database, DatabaseChangeLog changeLog)
            throws PreconditionFailedException, PreconditionErrorException {

        Statement statement = null;
        try {
            statement = ((JdbcConnection) database.getConnection())
                    .createStatement();

            String schemaName = getSchemaName();
            if (schemaName == null) {
                schemaName = database.getDefaultSchemaName();
            }
            String tableName = getTableName();
            String columnName = getColumnName();

            if (database instanceof PostgresDatabase) {
                String sql = "SELECT 1 FROM pg_attribute a WHERE EXISTS (SELECT 1 FROM pg_class JOIN pg_catalog.pg_namespace ns ON ns.oid = pg_class.relnamespace WHERE lower(ns.nspname)='"+schemaName.toLowerCase()+"' AND lower(relname) = lower('"+tableName+"') AND pg_class.oid = a.attrelid) AND lower(a.attname) = lower('"+columnName+"');";
                try {
                    ResultSet rs = statement.executeQuery(sql);
                    try {
                        if (rs.next()) {
                            return ;
                        } else {
                            // column or table does not exist
                            throw new PreconditionFailedException(format("Column %s.%s.%s does not exist", schemaName, tableName, columnName), changeLog, this);
                        }
                    } finally {
                        rs.close();
                    }
                } catch (SQLException e) {
                    throw new PreconditionErrorException(e, changeLog, this);
                }
            }

            try {
                String sql = format("select t.%s from %s.%s t where 0=1",
                        database.escapeColumnNameList(columnName),
                        database.escapeObjectName(schemaName, Schema.class),
                        database.escapeObjectName(tableName, Table.class));
                statement.executeQuery(sql).close();

                // column exists
                return;

            } catch (SQLException e) {
                // column or table does not exist
                throw new PreconditionFailedException(format(
                        "Column %s.%s.%s does not exist", schemaName,
                        tableName, columnName), changeLog, this);
            }

        } catch (DatabaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);

        } finally {
            JdbcUtils.closeStatement(statement);
        }
    }

    @Override
    public String getName() {
        return "columnExists";
    }
}
