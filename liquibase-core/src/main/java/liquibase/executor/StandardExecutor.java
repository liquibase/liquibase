package liquibase.executor;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.action.ExecuteAction;
import liquibase.action.QueryAction;
import liquibase.action.UpdateAction;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.statement.SqlStatement;

public class StandardExecutor extends AbstractExecutor {

    private Logger log = LogFactory.getLogger();

    @Override
    public boolean updatesDatabase() {
        return true;
    }

    @Override
    public QueryResult query(SqlStatement sql, ExecutionEnvironment env) throws DatabaseException {
        Action[] actions = StatementLogicFactory.getInstance().generateActions(sql, env);

        if (actions.length != 1) {
            throw new DatabaseException("Can only query with statements that return one executable");
        }

        Action action = actions[0];

        if (!(action instanceof QueryAction)) {
            throw new DatabaseException("Cannot query "+ action.getClass().getName());
        }

        return ((QueryAction) action).query(env);
    }


    @Override
    public ExecuteResult execute(SqlStatement sql, ExecutionEnvironment env) throws DatabaseException {
        Action[] actions = StatementLogicFactory.getInstance().generateActions(sql, env);
        for (Action action : actions) {
            if (!(action instanceof ExecuteAction)) {
                throw new DatabaseException("Cannot execute "+ action.getClass().getName());
            }
        }
        for (Action action : actions) {
            ((ExecuteAction) action).execute(env);
        }
        return new ExecuteResult();
    }


    @Override
    public UpdateResult update(SqlStatement sql, ExecutionEnvironment env) throws DatabaseException {
        Action[] actions = StatementLogicFactory.getInstance().generateActions(sql, env);

        if (actions.length != 1) {
            throw new DatabaseException("Can only update with statements that return one executable");
        }

        Action action = actions[0];

        if (!(action instanceof UpdateAction)) {
            throw new DatabaseException("Cannot update "+ action.getClass().getName());
        }

        return ((UpdateAction) action).update(env);
    }

    @Override
    public void comment(String message) throws DatabaseException {
        LogFactory.getLogger().debug(message);
    }
}
