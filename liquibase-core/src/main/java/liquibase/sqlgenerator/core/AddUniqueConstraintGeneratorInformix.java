package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
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
	public Sql[] generateSql(AddUniqueConstraintStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

		final String sqlNoContraintNameTemplate = "ALTER TABLE %s ADD CONSTRAINT UNIQUE (%s)";
		final String sqlTemplate = "ALTER TABLE %s ADD CONSTRAINT UNIQUE (%s) CONSTRAINT %s";
		if (statement.getConstraintName() == null) {
			return new Sql[] {
				new UnparsedSql(String.format(sqlNoContraintNameTemplate 
						, database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
						, database.escapeColumnNameList(statement.getColumnNames())
				))
			};
		} else {
			return new Sql[] {
				new UnparsedSql(String.format(sqlTemplate
						, database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
						, database.escapeColumnNameList(statement.getColumnNames())
						, database.escapeConstraintName(statement.getConstraintName())
				))
			};
		}


		
	}


}
