package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.SetColumnRemarksStatement;

public class SetColumnRemarksGenerator extends AbstractSqlGenerator<SetColumnRemarksStatement> {
	@Override
	public int getPriority() {
		return PRIORITY_DEFAULT;
	}

	@Override
	public boolean supports(SetColumnRemarksStatement statement, ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();

        return database instanceof OracleDatabase || database instanceof PostgresDatabase || database instanceof DB2Database;
	}

	@Override
    public ValidationErrors validate(SetColumnRemarksStatement setColumnRemarksStatement, ExecutionEnvironment env, StatementLogicChain chain) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.checkRequiredField("tableName", setColumnRemarksStatement.getTableName());
		validationErrors.checkRequiredField("columnName", setColumnRemarksStatement.getColumnName());
		validationErrors.checkRequiredField("remarks", setColumnRemarksStatement.getRemarks());
		return validationErrors;
	}

    @Override
    public Action[] generateActions(SetColumnRemarksStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        Database database = env.getTargetDatabase();

        return new Action[] { new UnparsedSql("COMMENT ON COLUMN " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
				+ "." + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " IS '"
				+ database.escapeStringForDatabase(statement.getRemarks()) + "'") };
	}
}
