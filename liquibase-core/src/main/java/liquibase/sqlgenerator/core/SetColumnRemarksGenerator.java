package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SetColumnRemarksStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class SetColumnRemarksGenerator extends AbstractSqlGenerator<SetColumnRemarksStatement> {
	@Override
	public int getPriority() {
		return PRIORITY_DEFAULT;
	}

	@Override
	public boolean supports(SetColumnRemarksStatement statement, Database database) {
		return database instanceof OracleDatabase || database instanceof PostgresDatabase || database instanceof DB2Database;
	}

	@Override
    public ValidationErrors validate(SetColumnRemarksStatement setColumnRemarksStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.checkRequiredField("tableName", setColumnRemarksStatement.getTableName());
		validationErrors.checkRequiredField("columnName", setColumnRemarksStatement.getColumnName());
		validationErrors.checkRequiredField("remarks", setColumnRemarksStatement.getRemarks());
		return validationErrors;
	}

	@Override
    public Sql[] generateSql(SetColumnRemarksStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
		return new Sql[] { new UnparsedSql("COMMENT ON COLUMN " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
				+ "." + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " IS '"
				+ database.escapeStringForDatabase(statement.getRemarks()) + "'", getAffectedColumn(statement)) };
	}

    protected Column getAffectedColumn(SetColumnRemarksStatement statement) {
        return new Column().setName(statement.getColumnName()).setRelation(new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName()));
    }
}
