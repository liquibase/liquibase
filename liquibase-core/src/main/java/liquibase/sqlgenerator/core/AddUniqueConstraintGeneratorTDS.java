package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
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
	public boolean supports(AddUniqueConstraintStatement statement, Database database) {
        return  (database instanceof MSSQLDatabase)
			|| (database instanceof SybaseDatabase)
			|| (database instanceof SybaseASADatabase)
		;
	}

	@Override
	public Sql[] generateSql(AddUniqueConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

		final String sqlTemplate = "ALTER TABLE %s ADD CONSTRAINT %s UNIQUE (%s)";
		final String sqlNoConstraintNameTemplate = "ALTER TABLE %s ADD UNIQUE (%s)";
		
		if (statement.getConstraintName() == null) {
			return new Sql[] {
				new UnparsedSql(String.format(sqlNoConstraintNameTemplate
						, database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
						, database.escapeColumnNameList(statement.getColumnNames())
				), getAffectedUniqueConstraint(statement))
			};
		} else {
			return new Sql[] {
				new UnparsedSql(String.format(sqlTemplate
						, database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
						, database.escapeConstraintName(statement.getConstraintName())
						, database.escapeColumnNameList(statement.getColumnNames())
				), getAffectedUniqueConstraint(statement))
			};
		}
	}


}
