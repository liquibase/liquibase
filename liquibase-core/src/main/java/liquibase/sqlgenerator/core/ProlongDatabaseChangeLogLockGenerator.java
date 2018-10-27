package liquibase.sqlgenerator.core;

import java.sql.Timestamp;

import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.ProlongDatabaseChangeLogLockStatement;
import liquibase.statement.core.UpdateStatement;

public class ProlongDatabaseChangeLogLockGenerator extends AbstractSqlGenerator<ProlongDatabaseChangeLogLockStatement> {

    @Override
    public ValidationErrors validate(ProlongDatabaseChangeLogLockStatement statement,
                                     Database database,
                                     SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(ProlongDatabaseChangeLogLockStatement statement, Database database,
                             SqlGeneratorChain sqlGeneratorChain) {
        String liquibaseSchema = database.getLiquibaseSchemaName();
        String liquibaseCatalog = database.getLiquibaseCatalogName();

        UpdateStatement updateStatement = new UpdateStatement(liquibaseCatalog, liquibaseSchema,
            database
                .getDatabaseChangeLogLockTableName());

        updateStatement.addNewColumnValue("LOCKEXPIRES",
            new Timestamp(statement.getLockExpiresOnServer().getTime()));

        setWhere(statement, database, liquibaseSchema, liquibaseCatalog, updateStatement);

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);

    }

    private void setWhere(ProlongDatabaseChangeLogLockStatement statement, Database database,
                          String liquibaseSchema, String liquibaseCatalog,
                          UpdateStatement updateStatement) {

        String idColumnName = database.escapeColumnName(
            liquibaseCatalog,
            liquibaseSchema,
            database.getDatabaseChangeLogTableName(),
            "ID");

        String lockedColumnName = database.escapeColumnName(
            liquibaseCatalog,
            liquibaseSchema,
            database.getDatabaseChangeLogTableName(),
            "LOCKED");

        String lockExpiresColumnName = database.escapeColumnName(
            liquibaseCatalog,
            liquibaseSchema,
            database.getDatabaseChangeLogTableName(),
            "LOCKEXPIRES");

        String lockedByIdColumnName = database.escapeColumnName(
            liquibaseCatalog,
            liquibaseSchema,
            database.getDatabaseChangeLogTableName(),
            "LOCKEDBYID");

        String convertedTrue = DataTypeFactory
            .getInstance()
            .fromDescription("boolean", database)
            .objectToSql(true, database);

        updateStatement.setWhereClause(
            idColumnName + " = 1 AND "
                // and is still locked
                + lockedColumnName + " = " + convertedTrue + " AND "
                // and is locked by a prolonging lock service (otherwise this field is NULL)
                + lockExpiresColumnName + " IS NOT NULL AND "
                // and is actually locked by us
                + lockedByIdColumnName + " = '" + statement.getLockedById() + "';");
    }
}