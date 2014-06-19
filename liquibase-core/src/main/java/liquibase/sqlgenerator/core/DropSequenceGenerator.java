package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropSequenceStatement;

public class DropSequenceGenerator extends AbstractSqlGenerator<DropSequenceStatement> {

    @Override
    public boolean supports(DropSequenceStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase().supportsSequences();
    }

    @Override
    public ValidationErrors validate(DropSequenceStatement dropSequenceStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("sequenceName", dropSequenceStatement.getSequenceName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropSequenceStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        String sql = "DROP SEQUENCE " + database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName());
        if (database instanceof PostgresDatabase) {
            sql += " CASCADE";
        }
        if (database instanceof DerbyDatabase) {
            sql += " RESTRICT";
        }
        return new Sql[] {
                new UnparsedSql(sql)
        };
    }
}
