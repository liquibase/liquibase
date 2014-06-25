package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.AddUniqueConstraintStatement;

public class AddUniqueConstraintGeneratorTDS extends AddUniqueConstraintGenerator {

	public AddUniqueConstraintGeneratorTDS() {

    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
	public boolean supports(AddUniqueConstraintStatement statement, ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();

        return  (database instanceof MSSQLDatabase)
			|| (database instanceof SybaseDatabase)
			|| (database instanceof SybaseASADatabase)
		;
	}

    @Override
    public Action[] generateActions(AddUniqueConstraintStatement statement, ExecutionEnvironment env, ActionGeneratorChain chain) {
        Database database = env.getTargetDatabase();

        final String sqlTemplate = "ALTER TABLE %s ADD CONSTRAINT %s UNIQUE (%s)";
		final String sqlNoContraintNameTemplate = "ALTER TABLE %s ADD UNIQUE (%s)";
		
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
						, database.escapeConstraintName(statement.getConstraintName())
						, database.escapeColumnNameList(statement.getColumnNames())
				))
			};
		}
	}


}
