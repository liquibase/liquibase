package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.core.AddUniqueConstraintStatement;

public class AddUniqueConstraintGeneratorInformix extends AddUniqueConstraintGenerator {

	public AddUniqueConstraintGeneratorInformix() {

    }

    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
	public boolean supports(AddUniqueConstraintStatement statement, ExecutionOptions options) {
        return (options.getRuntimeEnvironment().getTargetDatabase() instanceof InformixDatabase);
	}

    @Override
    public Action[] generateActions(AddUniqueConstraintStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

		final String sqlNoContraintNameTemplate = "ALTER TABLE %s ADD CONSTRAINT UNIQUE (%s)";
		final String sqlTemplate = "ALTER TABLE %s ADD CONSTRAINT UNIQUE (%s) CONSTRAINT %s";
		if (statement.getConstraintName() == null) {
			return new Action[] {
				new UnparsedSql(String.format(sqlNoContraintNameTemplate 
						, database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
						, database.escapeColumnNameList(statement.getColumnNames())
				))
			};
		} else {
			return new Action[] {
				new UnparsedSql(String.format(sqlTemplate
						, database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
						, database.escapeColumnNameList(statement.getColumnNames())
						, database.escapeConstraintName(statement.getConstraintName())
				))
			};
		}


		
	}


}
