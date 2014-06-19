package liquibase.sqlgenerator.core;

import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RuntimeStatement;

public class RuntimeGenerator extends AbstractSqlGenerator<RuntimeStatement> {

    @Override
    public ValidationErrors validate(RuntimeStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(RuntimeStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return statement.generate(options);
    }
}
