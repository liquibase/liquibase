package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.exception.UnsupportedException;
import liquibase.statement.core.DeleteDataStatement;
import liquibase.statement.core.InsertDataStatement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.InitializeDatabaseChangeLogLockTableStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InitializeDatabaseChangeLogLockTableGenerator extends AbstractSqlGenerator<InitializeDatabaseChangeLogLockTableStatement> {

    @Override
    public ValidationErrors validate(InitializeDatabaseChangeLogLockTableStatement initializeDatabaseChangeLogLockTableStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(InitializeDatabaseChangeLogLockTableStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        DeleteDataStatement deleteDataStatement = new DeleteDataStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName());
        InsertDataStatement insertDataStatement = new InsertDataStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .addColumnValue("ID", 1)
                .addColumnValue("LOCKED", Boolean.FALSE);

        List<Action> actions = new ArrayList<Action>();

        actions.addAll(Arrays.asList(StatementLogicFactory.getInstance().generateActions(deleteDataStatement, env)));
        actions.addAll(Arrays.asList(StatementLogicFactory.getInstance().generateActions(insertDataStatement, env)));

        return actions.toArray(new Action[actions.size()]);
    }
}
