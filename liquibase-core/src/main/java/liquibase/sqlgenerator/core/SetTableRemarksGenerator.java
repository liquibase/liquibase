package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SetTableRemarksStatement;

public class SetTableRemarksGenerator extends AbstractSqlGenerator<SetTableRemarksStatement> {

	@Override
	public boolean supports(SetTableRemarksStatement statement, ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

		return database instanceof MySQLDatabase || database instanceof OracleDatabase || database instanceof PostgresDatabase
				|| database instanceof DB2Database;
	}

	@Override
    public ValidationErrors validate(SetTableRemarksStatement setTableRemarksStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.checkRequiredField("tableName", setTableRemarksStatement.getTableName());
		validationErrors.checkRequiredField("remarks", setTableRemarksStatement.getRemarks());
		return validationErrors;
	}

	@Override
    public Sql[] generateSql(SetTableRemarksStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
		String sql;
		String remarks = database.escapeStringForDatabase(statement.getRemarks());
		if (database instanceof MySQLDatabase) {
			sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " COMMENT = '" + remarks
					+ "'";
		} else {
			String command = "COMMENT";

			sql = command + " ON TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " IS '"
					+ database.escapeStringForDatabase(remarks) + "'";
		}

		return new Sql[] { new UnparsedSql(sql) };
	}
}
