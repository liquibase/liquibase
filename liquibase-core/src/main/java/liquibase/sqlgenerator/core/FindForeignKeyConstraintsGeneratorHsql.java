package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.core.HsqlDatabase;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;

public class FindForeignKeyConstraintsGeneratorHsql extends AbstractSqlGenerator<FindForeignKeyConstraintsStatement> {
	@Override
	public int getPriority() {
		return PRIORITY_DATABASE;
	}

	@Override
	public boolean supports(FindForeignKeyConstraintsStatement statement, ExecutionEnvironment env) {
		return env.getTargetDatabase() instanceof HsqlDatabase;
	}

    @Override
    public boolean generateStatementsIsVolatile(ExecutionEnvironment env) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(ExecutionEnvironment env) {
        return false;
    }

    public ValidationErrors validate(FindForeignKeyConstraintsStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.checkRequiredField("baseTableName", statement.getBaseTableName());
		return validationErrors;
	}

    @Override
    public Action[] generateActions(FindForeignKeyConstraintsStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT ");
		sb.append("FKTABLE_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME).append(", ");
		sb.append("FKCOLUMN_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_COLUMN_NAME).append(", ");
		sb.append("PKTABLE_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_TABLE_NAME).append(", ");
		sb.append("PKCOLUMN_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_FOREIGN_COLUMN_NAME).append(", ");
		sb.append("FK_NAME as ").append(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME).append(" ");
		sb.append("FROM INFORMATION_SCHEMA.SYSTEM_CROSSREFERENCE ");
		sb.append("WHERE FKTABLE_NAME = '").append(statement.getBaseTableName().toUpperCase()).append("'");

		return new Action[] { new UnparsedSql(sb.toString()) };
	}
}
