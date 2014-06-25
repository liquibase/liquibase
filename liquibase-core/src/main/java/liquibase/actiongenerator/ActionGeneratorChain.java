package liquibase.actiongenerator;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.SqlStatement;

import java.util.Iterator;
import java.util.SortedSet;

public class ActionGeneratorChain {
    private Iterator<ActionGenerator> actionGenerators;

    public ActionGeneratorChain(SortedSet<ActionGenerator> actionGenerators) {
        if (actionGenerators != null) {
            this.actionGenerators = actionGenerators.iterator();
        }
    }

    public Action[] generateActions(SqlStatement statement, ExecutionEnvironment env) {
        if (actionGenerators == null) {
            return null;
        }

        if (!actionGenerators.hasNext()) {
            return new Action[0];
        }

        return actionGenerators.next().generateActions(statement, env, this);
    }

    public Warnings warn(SqlStatement statement, ExecutionEnvironment env) {
        if (actionGenerators == null || !actionGenerators.hasNext()) {
            return new Warnings();
        }

        return actionGenerators.next().warn(statement, env, this);
    }

    public ValidationErrors validate(SqlStatement statement, ExecutionEnvironment env) {
        if (actionGenerators == null || !actionGenerators.hasNext()) {
            return new ValidationErrors();
        }

        return actionGenerators.next().validate(statement, env, this);
    }
}
