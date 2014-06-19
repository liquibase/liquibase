package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.executor.ExecutionOptions;
import liquibase.datatype.DataTypeFactory;
import liquibase.database.core.InformixDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddAutoIncrementStatement;

public class AddAutoIncrementGeneratorInformix extends AddAutoIncrementGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof InformixDatabase;
    }

    @Override
    public ValidationErrors validate(
    		AddAutoIncrementStatement addAutoIncrementStatement,
    		ExecutionOptions options,
    		SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(
        	addAutoIncrementStatement, options, sqlGeneratorChain);

        validationErrors.checkRequiredField(
        	"columnDataType", addAutoIncrementStatement.getColumnDataType());

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
            		+ " MODIFY "
            		+ database.escapeColumnName(
                        statement.getCatalogName(),
            			statement.getSchemaName(),
            			statement.getTableName(),
            			statement.getColumnName())
            		+ " "
            		+ DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType() + "{autoIncrement:true}", database).toDatabaseDataType(database))
        };
    }
}

