package liquibase.sqlgenerator.core;

import static liquibase.sqlgenerator.core.LockDatabaseChangeLogGenerator.hostDescription;
import static liquibase.sqlgenerator.core.LockDatabaseChangeLogGenerator.hostaddress;
import static liquibase.sqlgenerator.core.LockDatabaseChangeLogGenerator.hostname;

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
    public ValidationErrors validate(ProlongDatabaseChangeLogLockStatement statement, Database database,
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

        updateStatement.addNewColumnValue("LOCKPROLONGED",
            new Timestamp(new java.util.Date().getTime()));

        updateStatement.setWhereClause(database.escapeColumnName(liquibaseCatalog,
            liquibaseSchema, database
            .getDatabaseChangeLogTableName(), "ID") + " = 1 AND " + database.escapeColumnName(liquibaseCatalog, liquibaseSchema, database
            .getDatabaseChangeLogTableName(), "LOCKED") + " = " + DataTypeFactory
            .getInstance()
            .fromDescription("boolean", database)
            .objectToSql(true, database) + " AND LOCKPROLONGED IS NOT NULL AND LOCKEDBY = '" + hostname + hostDescription + " (" + hostaddress + ")" +
            "';" );

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);

    }
}