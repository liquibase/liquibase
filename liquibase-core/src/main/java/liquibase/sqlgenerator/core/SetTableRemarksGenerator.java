package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.SetTableRemarksStatement;

public class SetTableRemarksGenerator extends AbstractSqlGenerator<SetTableRemarksStatement> {

	@Override
	public boolean supports(SetTableRemarksStatement statement, ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();

		return database instanceof MySQLDatabase || database instanceof OracleDatabase || database instanceof PostgresDatabase
				|| database instanceof DB2Database;
	}

	@Override
    public ValidationErrors validate(SetTableRemarksStatement setTableRemarksStatement, ExecutionEnvironment env, StatementLogicChain chain) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.checkRequiredField("tableName", setTableRemarksStatement.getTableName());
		validationErrors.checkRequiredField("remarks", setTableRemarksStatement.getRemarks());
		return validationErrors;
	}

    @Override
    public Action[] generateActions(SetTableRemarksStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();
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

		return new Action[] { new UnparsedSql(sql) };
	}
}
