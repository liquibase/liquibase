package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddFulltextConstraintStatement;

public class AddFulltextConstraintGeneratorTDS extends AddFulltextConstraintGenerator {

	public AddFulltextConstraintGeneratorTDS() {

    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
	public boolean supports(AddFulltextConstraintStatement statement, Database database) {
        return  (database instanceof MSSQLDatabase)
			|| (database instanceof SybaseDatabase)
			|| (database instanceof SybaseASADatabase)
		;
	}

	@Override
	public Sql[] generateSql(AddFulltextConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

		final String sqlTemplate = "ALTER TABLE %s ADD CONSTRAINT %s FULLTEXT (%s)";
		final String sqlNoContraintNameTemplate = "ALTER TABLE %s ADD FULLTEXT (%s)";
		
		if (statement.getConstraintName() == null) {
			return new Sql[] {
				new UnparsedSql(String.format(sqlNoContraintNameTemplate 
						, database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
						, database.escapeColumnNameList(statement.getColumnNames())
				), getAffectedFulltextConstraint(statement))
			};
		} else {
			return new Sql[] {
				new UnparsedSql(String.format(sqlTemplate
						, database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
						, database.escapeConstraintName(statement.getConstraintName())
						, database.escapeColumnNameList(statement.getColumnNames())
				), getAffectedFulltextConstraint(statement))
			};
		}
	}


}
