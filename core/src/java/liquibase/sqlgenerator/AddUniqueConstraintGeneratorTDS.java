package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.SybaseASADatabase;
import liquibase.database.SybaseDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.AddUniqueConstraintStatement;

public class AddUniqueConstraintGeneratorTDS extends AddUniqueConstraintGenerator {

	public AddUniqueConstraintGeneratorTDS() {

    }

    @Override
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    @Override
	public boolean isValidGenerator(AddUniqueConstraintStatement statement, Database database) {
        return  (database instanceof MSSQLDatabase)
			|| (database instanceof SybaseDatabase)
			|| (database instanceof SybaseASADatabase)
		;
	}

	@Override
	public Sql[] generateSql(AddUniqueConstraintStatement statement, Database database) {

		final String sqlTemplate = "ALTER TABLE %s ADD CONSTRAINT %s UNIQUE (%s)";
		final String sqlNoContraintNameTemplate = "ALTER TABLE %s ADD UNIQUE (%s)";
		
		if (statement.getConstraintName() == null) {
			return new Sql[] {
				new UnparsedSql(String.format(sqlNoContraintNameTemplate 
						, database.escapeTableName(statement.getSchemaName(), statement.getTableName())
						, database.escapeColumnNameList(statement.getColumnNames())
				))
			};
		} else {
			return new Sql[] {
				new UnparsedSql(String.format(sqlTemplate
						, database.escapeTableName(statement.getSchemaName(), statement.getTableName())
						, database.escapeConstraintName(statement.getConstraintName())
						, database.escapeColumnNameList(statement.getColumnNames())
				))
			};
		}
	}


}
