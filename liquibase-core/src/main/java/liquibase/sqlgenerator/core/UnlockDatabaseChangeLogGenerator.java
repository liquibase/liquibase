package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.exception.UnsupportedException;
import liquibase.statement.core.UpdateDataStatement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.UnlockDatabaseChangeLogStatement;

public class UnlockDatabaseChangeLogGenerator extends AbstractSqlGenerator<UnlockDatabaseChangeLogStatement> {

    @Override
    public ValidationErrors validate(UnlockDatabaseChangeLogStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(UnlockDatabaseChangeLogStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();
        String liquibaseSchema = database.getLiquibaseSchemaName();

        UpdateDataStatement releaseStatement = new UpdateDataStatement(database.getLiquibaseCatalogName(), liquibaseSchema, database.getDatabaseChangeLogLockTableName());
        releaseStatement.addNewColumnValue("LOCKED", false);
        releaseStatement.addNewColumnValue("LOCKGRANTED", null);
        releaseStatement.addNewColumnValue("LOCKEDBY", null);
        releaseStatement.setWhere(database.escapeColumnName(database.getLiquibaseCatalogName(), liquibaseSchema, database.getDatabaseChangeLogTableName(), "ID")+" = 1");

        return StatementLogicFactory.getInstance().generateActions(releaseStatement, env);
    }
}
