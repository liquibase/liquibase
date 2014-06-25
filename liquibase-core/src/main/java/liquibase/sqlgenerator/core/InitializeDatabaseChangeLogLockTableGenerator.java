package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.InitializeDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.InsertStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InitializeDatabaseChangeLogLockTableGenerator extends AbstractSqlGenerator<InitializeDatabaseChangeLogLockTableStatement> {

    @Override
    public ValidationErrors validate(InitializeDatabaseChangeLogLockTableStatement initializeDatabaseChangeLogLockTableStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(InitializeDatabaseChangeLogLockTableStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        Database database = env.getTargetDatabase();

        DeleteStatement deleteStatement = new DeleteStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName());
        InsertStatement insertStatement = new InsertStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .addColumnValue("ID", 1)
                .addColumnValue("LOCKED", Boolean.FALSE);

        List<Action> actions = new ArrayList<Action>();

        actions.addAll(Arrays.asList(StatementLogicFactory.getInstance().generateActions(deleteStatement, env)));
        actions.addAll(Arrays.asList(StatementLogicFactory.getInstance().generateActions(insertStatement, env)));

        return actions.toArray(new Action[actions.size()]);
    }
}
