package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.AddAutoIncrementStatement;

public class AddAutoIncrementGeneratorHsqlH2 extends AddAutoIncrementGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        return database instanceof HsqlDatabase || database instanceof H2Database;
    }

    @Override
    public Action[] generateActions(AddAutoIncrementStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        return new Action[]{
            new UnparsedSql(
            	"ALTER TABLE "
            		+ database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
            		+ " ALTER COLUMN "
            		+ database.escapeColumnName(
                        statement.getCatalogName(),
            			statement.getSchemaName(),
            			statement.getTableName(),
            			statement.getColumnName())
            		+ " "
            		+ DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(), database)
            		+ " "
            		+ database.getAutoIncrementClause(
            			statement.getStartWith(), statement.getIncrementBy()))
        };
    }
}
