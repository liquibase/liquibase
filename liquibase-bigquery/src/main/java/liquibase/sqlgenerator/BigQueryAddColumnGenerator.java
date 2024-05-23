package liquibase.sqlgenerator;

import liquibase.Scope;
import liquibase.database.BigQueryDatabase;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.DatabaseDataType;
import liquibase.sqlgenerator.core.AddColumnGenerator;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.core.AddColumnStatement;

public class BigQueryAddColumnGenerator extends AddColumnGenerator {

	@Override
	protected String generateSingleColumnSQL(AddColumnStatement statement, Database database) {
		DatabaseDataType columnType = null;

		if (statement.getColumnType() != null) {
			columnType = DataTypeFactory.getInstance().fromDescription(statement.getColumnType() + (statement.isAutoIncrement() ? "{autoIncrement:true}" : ""), database).toDatabaseDataType(database);
		}

		String alterTable = " ADD COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName());

		if (columnType != null) {
			alterTable += " " + columnType;
		}

		if (statement.isAutoIncrement() && database.supportsAutoIncrement()) {
			AutoIncrementConstraint autoIncrementConstraint = statement.getAutoIncrementConstraint();
			alterTable += " " + database.getAutoIncrementClause(autoIncrementConstraint.getStartWith(), autoIncrementConstraint.getIncrementBy(), autoIncrementConstraint.getGenerationType(), autoIncrementConstraint.getDefaultOnNull());
		}

		if (statement.getDefaultValue() != null) {
			// TODO add default value
			// Add field with default value to an existing table schema is not supported.
			// You can achieve the same result by executing the following 3 statements:
			// 1. ALTER TABLE tableName ADD COLUMN columnName;
			// 2. ALTER TABLE tableName ALTER COLUMN columnName SET DEFAULT columnDefaultValue;
			// 3. UPDATE tableName SET columnName = columnDefaultValue WHERE TRUE;
			Scope.getCurrentScope().getLog(this.getClass()).fine("Default clauses are not supported during column creation by BigQuery");
		}

		if (!statement.isNullable()) {
			Scope.getCurrentScope().getLog(this.getClass()).fine("Not null constraints are not supported by BigQuery");
		}

		if (statement.isPrimaryKey()) {
			String baseSQL = generateSingleColumBaseSQL(statement, database);
			alterTable += "; " + baseSQL + " ADD PRIMARY KEY ("
					+ database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName())
					+ ") NOT ENFORCED";
		}

		if ((statement.getAddBeforeColumn() != null) && !statement.getAddBeforeColumn().isEmpty()) {
			Scope.getCurrentScope().getLog(this.getClass()).fine("Before clauses are not supported by BigQuery");
		}

		if ((statement.getAddAfterColumn() != null) && !statement.getAddAfterColumn().isEmpty()) {
			Scope.getCurrentScope().getLog(this.getClass()).fine("After clauses are not supported by BigQuery");
		}

		return alterTable;
	}

	@Override
	public int getPriority() {
		return BigQueryDatabase.BIGQUERY_PRIORITY_DATABASE;
	}

	@Override
	public boolean supports(AddColumnStatement statement, Database database) {
		return database instanceof BigQueryDatabase;
	}
}
