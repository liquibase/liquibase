package liquibase.sqlgenerator.core;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.statement.core.InsertOrUpdateDataStatement;

public class InsertOrUpdateGeneratorPostgres extends InsertOrUpdateGenerator {
	@Override
    public boolean supports(InsertOrUpdateDataStatement statement, ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();

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
    public Action[] generateActions(InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        StringBuilder generatedSql = new StringBuilder();
        generatedSql.append("DO\n");
		generatedSql.append("$$\n");
		generatedSql.append("BEGIN\n");
		try {
			generatedSql.append(getUpdateStatement(insertOrUpdateDataStatement,
                    env, getWhereClause(insertOrUpdateDataStatement, env),
					chain));
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
					+ database.escapeTableName(insertOrUpdateDataStatement.getCatalogName(), insertOrUpdateDataStatement.getSchemaName(),
							insertOrUpdateDataStatement.getTableName()) + " WHERE " + getWhereClause(insertOrUpdateDataStatement, env) + ";\n");
		}
		generatedSql.append("IF not found THEN\n");
		generatedSql.append(getInsertStatement(insertOrUpdateDataStatement, env, chain));
		generatedSql.append("END IF;\n");
		generatedSql.append("END;\n");
		generatedSql.append("$$\n");
		generatedSql.append("LANGUAGE plpgsql;\n");
		return new Action[] { new UnparsedSql(generatedSql.toString()) };
	}

	@Override
	protected String getElse(ExecutionEnvironment env) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected String getRecordCheck(InsertOrUpdateDataStatement arg0,
                                    ExecutionEnvironment env, String arg2) {
		throw new UnsupportedOperationException();
	}
}

