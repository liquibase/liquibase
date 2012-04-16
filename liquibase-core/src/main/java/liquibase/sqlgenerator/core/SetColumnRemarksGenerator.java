package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DB2iDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SetColumnRemarksStatement;

public class SetColumnRemarksGenerator extends AbstractSqlGenerator<SetColumnRemarksStatement> {
	@Override
	public int getPriority() {
		return PRIORITY_DEFAULT;
	}

	@Override
	public boolean supports(SetColumnRemarksStatement statement, Database database) {
		return database instanceof OracleDatabase || database instanceof PostgresDatabase || database instanceof DB2Database;
	}

	public ValidationErrors validate(SetColumnRemarksStatement setColumnRemarksStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.checkRequiredField("tableName", setColumnRemarksStatement.getTableName());
		validationErrors.checkRequiredField("columnName", setColumnRemarksStatement.getColumnName());
		validationErrors.checkRequiredField("remarks", setColumnRemarksStatement.getRemarks());
		return validationErrors;
	}

	public Sql[] generateSql(SetColumnRemarksStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
		if (database instanceof DB2iDatabase) {
			return new Sql[] {
					new UnparsedSql("LABEL ON " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ("
							+ database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName())
							+ " TEXT IS '" + database.escapeStringForDatabase(statement.getRemarks()) + "')"),
					new UnparsedSql("LABEL ON COLUMN " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + "."
							+ database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName())
							+ " IS '" + database.escapeStringForDatabase(statement.getRemarks()) + "'") };
		}

		return new Sql[] { new UnparsedSql("COMMENT ON COLUMN " + database.escapeTableName(statement.getSchemaName(), statement.getTableName())
				+ "." + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " IS '"
				+ database.escapeStringForDatabase(statement.getRemarks()) + "'") };
	}
}
