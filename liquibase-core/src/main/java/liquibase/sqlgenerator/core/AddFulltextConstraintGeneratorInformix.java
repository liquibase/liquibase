package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddFulltextConstraintStatement;

public class AddFulltextConstraintGeneratorInformix extends AddFulltextConstraintGenerator {

	public AddFulltextConstraintGeneratorInformix() {

    }

    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
	public boolean supports(AddFulltextConstraintStatement statement, Database database) {
        return (database instanceof InformixDatabase);
	}

	@Override
	public Sql[] generateSql(AddFulltextConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

		final String sqlNoContraintNameTemplate = "ALTER TABLE %s ADD CONSTRAINT FULLTEXT (%s)";
		final String sqlTemplate = "ALTER TABLE %s ADD CONSTRAINT FULLTEXT (%s) CONSTRAINT %s";
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
						, database.escapeColumnNameList(statement.getColumnNames())
						, database.escapeConstraintName(statement.getConstraintName())
				), getAffectedFulltextConstraint(statement))
			};
		}


		
	}


}
