package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.Sql;
import liquibase.action.core.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.structure.core.Column;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adds functionality for setting the sequence to be owned by the column with the default value
 */
public class AddDefaultValueGeneratorPostgres extends AddDefaultValueGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();

        return database instanceof PostgresDatabase;
    }

    @Override
    public Action[] generateActions(AddDefaultValueStatement statement, ExecutionEnvironment env, ActionGeneratorChain chain) {
        Database database = env.getTargetDatabase();

        if (!(statement.getDefaultValue() instanceof SequenceNextValueFunction)) {
            return super.generateActions(statement, env, chain);
        }

        List<Action> commands = new ArrayList<Action>(Arrays.asList(super.generateActions(statement, env, chain)));
        // for postgres, we need to also set the sequence to be owned by this table for true serial like functionality.
        // this will allow a drop table cascade to remove the sequence as well.
        SequenceNextValueFunction sequenceFunction = (SequenceNextValueFunction) statement.getDefaultValue();

        Sql alterSequenceOwner = new UnparsedSql("ALTER SEQUENCE " + database.escapeSequenceName(statement.getCatalogName(),
                statement.getSchemaName(), sequenceFunction.getValue()) + " OWNED BY " +
                database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + "."
                + database.escapeObjectName(statement.getColumnName(), Column.class));
        commands.add(alterSequenceOwner);
        return commands.toArray(new Action[commands.size()]);
    }
}
