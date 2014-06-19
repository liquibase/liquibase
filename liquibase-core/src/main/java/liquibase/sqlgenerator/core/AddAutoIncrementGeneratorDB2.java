package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddAutoIncrementStatement;

public class AddAutoIncrementGeneratorDB2 extends AddAutoIncrementGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof DB2Database;
    }

    @Override
    public ValidationErrors validate(
            AddAutoIncrementStatement statement,
            ExecutionOptions options,
            SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("columnName", statement.getColumnName());
        validationErrors.checkRequiredField("tableName", statement.getTableName());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(
    		AddAutoIncrementStatement statement,
    		ExecutionOptions options,
    		SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        return new Sql[]{
            new UnparsedSql(
            	"ALTER TABLE "
            		+ database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
            		+ " ALTER COLUMN "
            		+ database.escapeColumnName(
                        statement.getCatalogName(),
            			statement.getSchemaName(),
            			statement.getTableName(),
            			statement.getColumnName())
            		+ " SET "
            		+ database.getAutoIncrementClause(
            			statement.getStartWith(), statement.getIncrementBy()))
        };
    }
}