package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.AddAutoIncrementStatement;

public class AddAutoIncrementGeneratorInformix extends AddAutoIncrementGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof InformixDatabase;
    }

    @Override
    public ValidationErrors validate(
    		AddAutoIncrementStatement addAutoIncrementStatement,
    		ExecutionEnvironment env,
    		ActionGeneratorChain chain) {
        ValidationErrors validationErrors = super.validate(
        	addAutoIncrementStatement, env, chain);

        validationErrors.checkRequiredField(
        	"columnDataType", addAutoIncrementStatement.getColumnDataType());

        return validationErrors;
    }

    @Override
    public Action[] generateActions(AddAutoIncrementStatement statement, ExecutionEnvironment env, ActionGeneratorChain chain) {
        Database database = env.getTargetDatabase();

        return new Action[]{
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

