package liquibase.sqlgenerator.core;

import java.sql.Timestamp;
import java.util.Date;

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

        if (statement.getMaxTTLInSeconds() < 1) {
            validationErrors.addError("maxTTL in seconds must be > 1");
        }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RemoveStaleLocksStatement statement, Database database,
                             SqlGeneratorChain sqlGeneratorChain) {

        long now = new Date().getTime();
        long maxTTLSecondsAgo = now - statement.getMaxTTLInSeconds() * 1000;

        String liquibaseSchema = database.getLiquibaseSchemaName();

        UpdateStatement updateStatement = new UpdateStatement(
            database.getLiquibaseCatalogName(),
            liquibaseSchema,
            database.getDatabaseChangeLogLockTableName());

        updateStatement.addNewColumnValue("LOCKED", false);
        updateStatement.addNewColumnValue("LOCKGRANTED", null);
        updateStatement.addNewColumnValue("LOCKPROLONGED", null);
        updateStatement.addNewColumnValue("LOCKEDBY", null);

        updateStatement
            // remove where lock has expired ...
            .setWhereClause("ID = 1 AND LOCKED = :value AND LOCKPROLONGED < :value" +
                // ... but only when lock was set by ProlongingLockService, otherwise do not remove
                // in order to stay compatible
                " AND LOCKPROLONGED IS NOT NULL");
        updateStatement.addWhereParameter(true);
        updateStatement.addWhereParameter(new Timestamp(maxTTLSecondsAgo));

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);
    }
}