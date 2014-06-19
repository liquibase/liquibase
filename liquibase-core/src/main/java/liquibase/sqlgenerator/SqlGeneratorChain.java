package liquibase.sqlgenerator;

import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;

public class SqlGeneratorChain {

    private ActionGeneratorChain delegate;

    public SqlGeneratorChain(ActionGeneratorChain actionGeneratorChain) {
        this.delegate = actionGeneratorChain;
    }

    public Sql[] generateSql(SqlStatement statement, ExecutionOptions options) {
        return (Sql[]) delegate.generateActions(statement, options);
    }

    public Warnings warn(SqlStatement statement, ExecutionOptions options) {
        return delegate.warn(statement, options);
    }

    public ValidationErrors validate(SqlStatement statement, ExecutionOptions options) {
        return delegate.validate(statement, options);
    }
}
