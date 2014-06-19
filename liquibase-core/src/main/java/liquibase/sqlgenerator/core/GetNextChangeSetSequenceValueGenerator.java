package liquibase.sqlgenerator.core;

import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.GetNextChangeSetSequenceValueStatement;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;

public class GetNextChangeSetSequenceValueGenerator extends AbstractSqlGenerator<GetNextChangeSetSequenceValueStatement> {

    @Override
    public ValidationErrors validate(GetNextChangeSetSequenceValueStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(GetNextChangeSetSequenceValueStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return SqlGeneratorFactory.getInstance().generateSql(new SelectFromDatabaseChangeLogStatement("MAX(ORDEREXECUTED)"), options);
    }
}
