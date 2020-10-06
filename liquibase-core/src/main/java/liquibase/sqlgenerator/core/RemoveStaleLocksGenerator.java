package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.RemoveStaleLocksStatement;
import liquibase.statement.core.UpdateStatement;

public class RemoveStaleLocksGenerator extends AbstractSqlGenerator<RemoveStaleLocksStatement> {


    @Override
    public ValidationErrors validate(RemoveStaleLocksStatement statement, Database database,
                                     SqlGeneratorChain sqlGeneratorChain) {

        ValidationErrors validationErrors = new ValidationErrors();

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RemoveStaleLocksStatement statement, Database database,
                             SqlGeneratorChain sqlGeneratorChain) {

        String liquibaseSchema = database.getLiquibaseSchemaName();

        UpdateStatement updateStatement = new UpdateStatement(
            database.getLiquibaseCatalogName(),
            liquibaseSchema,
            database.getDatabaseChangeLogLockTableName());

        updateStatement.addNewColumnValue("LOCKED", false);
        updateStatement.addNewColumnValue("LOCKGRANTED", null);
        updateStatement.addNewColumnValue("LOCKEXPIRES", null);
        updateStatement.addNewColumnValue("LOCKEDBY", null);
        updateStatement.addNewColumnValue("LOCKEDBYID", null);

        updateStatement
            // remove lock ...
            .setWhereClause("ID = 1 AND " +
                " LOCKED = :value " +
                // ... when lock has expired AND was set by ProlongingLockService,
                // otherwise do not remove in order to stay compatible
                " AND LOCKEXPIRES < " + database.getCurrentDateTimeFunction() +
                " AND LOCKEXPIRES IS NOT NULL");
        updateStatement.addWhereParameter(true);

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);
    }
}