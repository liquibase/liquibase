package liquibase.executor;

import liquibase.action.Action;
import liquibase.action.ExecuteAction;
import liquibase.action.QueryAction;
import liquibase.action.UpdateAction;
import liquibase.actiongenerator.ActionGeneratorFactory;
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
    public QueryResult query(SqlStatement sql, ExecutionOptions options) throws DatabaseException {
        Action[] actions = ActionGeneratorFactory.getInstance().generateActions(sql, options);

        if (actions.length != 1) {
            throw new DatabaseException("Can only query with statements that return one executable");
        }

        Action action = actions[0];

        if (!(action instanceof QueryAction)) {
            throw new DatabaseException("Cannot query "+ action.getClass().getName());
        }

        return ((QueryAction) action).query(options);
    }


    @Override
    public ExecuteResult execute(SqlStatement sql, ExecutionOptions options) throws DatabaseException {
        Action[] actions = ActionGeneratorFactory.getInstance().generateActions(sql, options);
        for (Action action : actions) {
            if (!(action instanceof ExecuteAction)) {
                throw new DatabaseException("Cannot execute "+ action.getClass().getName());
            }
        }
        for (Action action : actions) {
            ((ExecuteAction) action).execute(options);
        }
        return new ExecuteResult();
    }


    @Override
    public UpdateResult update(SqlStatement sql, ExecutionOptions options) throws DatabaseException {
        Action[] actions = ActionGeneratorFactory.getInstance().generateActions(sql, options);

        if (actions.length != 1) {
            throw new DatabaseException("Can only update with statements that return one executable");
        }

        Action action = actions[0];

        if (!(action instanceof UpdateAction)) {
            throw new DatabaseException("Cannot update "+ action.getClass().getName());
        }

        return ((UpdateAction) action).update(options);
    }

    @Override
    public void comment(String message) throws DatabaseException {
        LogFactory.getLogger().debug(message);
    }
}
