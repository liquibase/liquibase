package liquibase.statementlogic;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.statement.SqlStatement;

import java.util.Iterator;
import java.util.SortedSet;

public class StatementLogicChain {
    private Iterator<StatementLogic> statementLogicIterator;

    public StatementLogicChain(SortedSet<StatementLogic> statementLogic) {
        if (statementLogic != null) {
            this.statementLogicIterator = statementLogic.iterator();
        }
    }

    public Action[] generateActions(SqlStatement statement, ExecutionEnvironment env) {
        if (statementLogicIterator == null) {
            return null;
        }

        if (!statementLogicIterator.hasNext()) {
            return new Action[0];
        }

        return statementLogicIterator.next().generateActions(statement, env, this);
    }

    public Warnings warn(SqlStatement statement, ExecutionEnvironment env) {
        if (statementLogicIterator == null || !statementLogicIterator.hasNext()) {
            return new Warnings();
        }

        return statementLogicIterator.next().warn(statement, env, this);
    }

    public ValidationErrors validate(SqlStatement statement, ExecutionEnvironment env) {
        if (statementLogicIterator == null || !statementLogicIterator.hasNext()) {
            return new ValidationErrors();
        }

        return statementLogicIterator.next().validate(statement, env, this);
    }
}
