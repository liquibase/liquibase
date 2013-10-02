package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertOrUpdateStatement;

public class InsertOrUpdateGeneratorPostgres extends InsertOrUpdateGenerator {
	@Override
    public boolean supports(InsertOrUpdateStatement statement, Database database) {
		if (database instanceof PostgresDatabase) {
            try {
                return database.getDatabaseMajorVersion() >= 9;
            } catch (DatabaseException e) {
                return true;
            }
        }
        return false;
	}

	@Override
	public Sql[] generateSql(InsertOrUpdateStatement insertOrUpdateStatement,
			Database database, SqlGeneratorChain sqlGeneratorChain) {
		StringBuilder generatedSql = new StringBuilder();
		generatedSql.append("DO\n");
		generatedSql.append("$$\n");
		generatedSql.append("BEGIN\n");
		try {
			generatedSql.append(getUpdateStatement(insertOrUpdateStatement,
					database, getWhereClause(insertOrUpdateStatement, database),
					sqlGeneratorChain));
		} catch (LiquibaseException e) {
			// do a select statement instead
			/*
			generatedSql.append("select * from " + database.escapeTableName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName()) + " WHERE " +
					getWhereClause(insertOrUpdateStatement, database) + "\n");
			*/

			// The above code results in an invalid pl/pgsql statement as the select is not being stored.
			// The perform keyword can be used here as an alternative as it does not return a value.
			// Additionally the statement is not being terminated correctly, it is missing a semi-colon.
			generatedSql.append("perform * from "
					+ database.escapeTableName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(),
							insertOrUpdateStatement.getTableName()) + " WHERE " + getWhereClause(insertOrUpdateStatement, database) + ";\n");
		}
		generatedSql.append("IF not found THEN\n");
		generatedSql.append(getInsertStatement(insertOrUpdateStatement,
				database, sqlGeneratorChain));
		generatedSql.append("END IF;\n");
		generatedSql.append("END;\n");
		generatedSql.append("$$\n");
		generatedSql.append("LANGUAGE plpgsql;\n");
		return new Sql[] { new UnparsedSql(generatedSql.toString(), getAffectedTable(insertOrUpdateStatement)) };
	}

	@Override
	protected String getElse(Database arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected String getRecordCheck(InsertOrUpdateStatement arg0,
			Database arg1, String arg2) {
		throw new UnsupportedOperationException();
	}
}

