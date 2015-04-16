package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
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
	public boolean supports(AddUniqueConstraintStatement statement, Database database) {
        return (database instanceof InformixDatabase);
	}

	@Override
	public Sql[] generateSql(AddUniqueConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

		final String sqlNoConstraintNameTemplate = "ALTER TABLE %s ADD CONSTRAINT UNIQUE (%s)";
		final String sqlTemplate = "ALTER TABLE %s ADD CONSTRAINT UNIQUE (%s) CONSTRAINT %s";
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
						, database.escapeColumnNameList(statement.getColumnNames())
						, database.escapeConstraintName(statement.getConstraintName())
				), getAffectedUniqueConstraint(statement))
			};
		}


		
	}


}
