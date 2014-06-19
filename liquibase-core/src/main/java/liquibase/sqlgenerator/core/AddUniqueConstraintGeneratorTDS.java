package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddUniqueConstraintStatement;

public class AddUniqueConstraintGeneratorTDS extends AddUniqueConstraintGenerator {

	public AddUniqueConstraintGeneratorTDS() {

    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
	public boolean supports(AddUniqueConstraintStatement statement, ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        return  (database instanceof MSSQLDatabase)
			|| (database instanceof SybaseDatabase)
			|| (database instanceof SybaseASADatabase)
		;
	}

	@Override
	public Sql[] generateSql(AddUniqueConstraintStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {

        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        final String sqlTemplate = "ALTER TABLE %s ADD CONSTRAINT %s UNIQUE (%s)";
		final String sqlNoContraintNameTemplate = "ALTER TABLE %s ADD UNIQUE (%s)";
		
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
						, database.escapeConstraintName(statement.getConstraintName())
						, database.escapeColumnNameList(statement.getColumnNames())
				))
			};
		}
	}


}
