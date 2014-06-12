package liquibase.actiongenerator;

import liquibase.action.Action;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.sql.Sql;
import liquibase.actiongenerator.ActionGenerator;
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

    public Action[] generateActions(SqlStatement statement, Database database) {
        if (actionGenerators == null) {
            return null;
        }

        if (!actionGenerators.hasNext()) {
            return new Action[0];
        }

        return actionGenerators.next().generateActions(statement, database, this);
    }

    public Warnings warn(SqlStatement statement, Database database) {
        if (actionGenerators == null || !actionGenerators.hasNext()) {
            return new Warnings();
        }

        return actionGenerators.next().warn(statement, database, this);
    }

    public ValidationErrors validate(SqlStatement statement, Database database) {
        if (actionGenerators == null || !actionGenerators.hasNext()) {
            return new ValidationErrors();
        }

        return actionGenerators.next().validate(statement, database, this);
    }
}
