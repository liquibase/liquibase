package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.UnlockDatabaseChangeLogStatement;
import liquibase.statement.core.UpdateStatement;

public class UnlockDatabaseChangeLogGenerator extends AbstractSqlGenerator<UnlockDatabaseChangeLogStatement> {

    @Override
    public ValidationErrors validate(UnlockDatabaseChangeLogStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(UnlockDatabaseChangeLogStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
    	String liquibaseSchema = database.getLiquibaseSchemaName();

        UpdateStatement releaseStatement = new UpdateStatement(database.getLiquibaseCatalogName(), liquibaseSchema, database.getDatabaseChangeLogLockTableName());
        releaseStatement.addNewColumnValue("LOCKED", false);
        releaseStatement.addNewColumnValue("LOCKGRANTED", null);
        releaseStatement.addNewColumnValue("LOCKEDBY", null);
        releaseStatement.setWhereClause(database.escapeColumnName(database.getLiquibaseCatalogName(), liquibaseSchema, database.getDatabaseChangeLogTableName(), "ID")+" = 1");

        return SqlGeneratorFactory.getInstance().generateSql(releaseStatement, options);
    }
}
