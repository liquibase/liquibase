package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.LiquibaseException;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertOrUpdateStatement;

import java.util.Date;

/**
 * @author Andrew Muraco
 */
public class InsertOrUpdateGeneratorHsql extends InsertOrUpdateGenerator {
	@Override
	public boolean supports(InsertOrUpdateStatement statement, Database database) {
		return database instanceof HsqlDatabase;
	}

	@Override
	protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, Database database,
									String whereClause) {
		return "MERGE INTO " +
				insertOrUpdateStatement.getTableName() +
				" USING (VALUES (1)) ON " +
				whereClause +
				" WHEN NOT MATCHED THEN ";
	}

	@Override
	protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database,
										SqlGeneratorChain sqlGeneratorChain) {
		StringBuilder columns = new StringBuilder();
		StringBuilder values = new StringBuilder();

		for (String columnKey : insertOrUpdateStatement.getColumnValues().keySet()) {
			columns.append(",");
			columns.append(columnKey);
			values.append(",");
			values.append(convertToString(insertOrUpdateStatement.getColumnValue(columnKey), database));
		}
		columns.deleteCharAt(0);
		values.deleteCharAt(0);
		return "INSERT (" + columns + ") VALUES (" + values + ")";
	}

	@Override
	protected String getElse(Database database) {
		return " WHEN MATCHED THEN ";
	}

	@Override
	protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database,
										String whereClause, SqlGeneratorChain sqlGeneratorChain) throws LiquibaseException {

		if (insertOrUpdateStatement.getOnlyUpdate()) {
			return super.getUpdateStatement(insertOrUpdateStatement, database, whereClause, sqlGeneratorChain);
		}

		StringBuilder sql = new StringBuilder("UPDATE SET ");

		for (String columnKey : insertOrUpdateStatement.getColumnValues().keySet()) {
			if (insertOrUpdateStatement.getAllowColumnUpdate(columnKey)) {
				sql.append(columnKey).append(" = ");
				sql.append(convertToString(insertOrUpdateStatement.getColumnValue(columnKey), database));
				sql.append(",");
			}
		}
		int lastComma = sql.lastIndexOf(",");
		if (lastComma > -1) {
			sql.deleteCharAt(lastComma);
		}
		return sql.toString();
	}

	// Copied from liquibase.sqlgenerator.core.InsertOrUpdateGeneratorMySQL
	private String convertToString(Object newValue, Database database) {
		String sqlString;
		if ((newValue == null) || "".equals(newValue.toString()) || "NULL".equalsIgnoreCase(newValue.toString())) {
			sqlString = "NULL";
		} else if ((newValue instanceof String) && !looksLikeFunctionCall(((String) newValue), database)) {
			sqlString = "'" + database.escapeStringForDatabase(newValue.toString()) + "'";
		} else if (newValue instanceof Date) {
			sqlString = database.getDateLiteral(((Date) newValue));
		} else if (newValue instanceof Boolean) {
			if (((Boolean) newValue)) {
				sqlString = DataTypeFactory.getInstance().getTrueBooleanValue(database);
			} else {
				sqlString = DataTypeFactory.getInstance().getFalseBooleanValue(database);
			}
		} else {
			sqlString = newValue.toString();
		}
		return sqlString;
	}
}
