package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.DropSequenceStatement;

public class DropSequenceGenerator extends AbstractSqlGenerator<DropSequenceStatement> {

    @Override
    public boolean supports(DropSequenceStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase().supportsSequences();
    }

    @Override
    public ValidationErrors validate(DropSequenceStatement dropSequenceStatement, ExecutionEnvironment env, ActionGeneratorChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("sequenceName", dropSequenceStatement.getSequenceName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(DropSequenceStatement statement, ExecutionEnvironment env, ActionGeneratorChain chain) {
        Database database = env.getTargetDatabase();

        String sql = "DROP SEQUENCE " + database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName());
        if (database instanceof PostgresDatabase) {
            sql += " CASCADE";
        }
        if (database instanceof DerbyDatabase) {
            sql += " RESTRICT";
        }
        return new Action[] {
                new UnparsedSql(sql)
        };
    }
}
