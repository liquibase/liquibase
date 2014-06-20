package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.core.HsqlDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;

public class FindForeignKeyConstraintsGeneratorHsql extends AbstractSqlGenerator<FindForeignKeyConstraintsStatement> {
	@Override
	public int getPriority() {
		return PRIORITY_DATABASE;
	}

	@Override
	public boolean supports(FindForeignKeyConstraintsStatement statement, ExecutionOptions options) {
		return options.getRuntimeEnvironment().getTargetDatabase() instanceof HsqlDatabase;
	}

    @Override
    public boolean generateStatementsIsVolatile(ExecutionOptions options) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(ExecutionOptions options) {
        return false;
    }

    public ValidationErrors validate(FindForeignKeyConstraintsStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.checkRequiredField("baseTableName", statement.getBaseTableName());
		return validationErrors;
	}

    @Override
    public Action[] generateActions(FindForeignKeyConstraintsStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
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
