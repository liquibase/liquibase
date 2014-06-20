package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.logging.LogFactory;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropTableStatement;

public class DropTableGenerator extends AbstractSqlGenerator<DropTableStatement> {

    @Override
    public ValidationErrors validate(DropTableStatement dropTableStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropTableStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(DropTableStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        StringBuffer buffer = new StringBuffer();
        buffer.append("DROP TABLE ").append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));
        if (statement.isCascadeConstraints()) {
            if (!database.supportsDropTableCascadeConstraints()) {
                LogFactory.getLogger().warning("Database does not support drop with cascade");
            } else if (database instanceof OracleDatabase) {
                buffer.append(" CASCADE CONSTRAINTS");
            } else {
                buffer.append(" CASCADE");
            }
        }

        return new Action[]{
                new UnparsedSql(buffer.toString())
        };
    }
}
