package liquibase.sqlgenerator.core;

import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.InsertOrUpdateStatement;

import java.util.Date;

/**
 * @author Andrew Muraco
 */
public class InsertOrUpdateGeneratorHsql extends InsertOrUpdateGenerator {
	@Override
	public boolean supports(InsertOrUpdateStatement statement, ExecutionOptions options) {
		return options.getRuntimeEnvironment().getTargetDatabase() instanceof HsqlDatabase;
	}

	@Override
	protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionOptions options,
			String whereClause) {
		StringBuilder sql = new StringBuilder();
		sql.append("MERGE INTO ");
		sql.append(insertOrUpdateStatement.getTableName());
		sql.append(" USING (VALUES (1)) ON ");
		sql.append(whereClause);
		sql.append(" WHEN NOT MATCHED THEN ");
		return sql.toString();
	}

	@Override
	protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionOptions options,
			ActionGeneratorChain chain) {
		StringBuilder columns = new StringBuilder();
		StringBuilder values = new StringBuilder();

		for (String columnKey : insertOrUpdateStatement.getColumnValues().keySet()) {
			columns.append(",");
			columns.append(columnKey);
			values.append(",");
			values.append(convertToString(insertOrUpdateStatement.getColumnValue(columnKey), options.getRuntimeEnvironment().getTargetDatabase()));
		}
		columns.deleteCharAt(0);
		values.deleteCharAt(0);
		return "INSERT (" + columns.toString() + ") VALUES (" + values.toString() + ")";
	}

	@Override
	protected String getElse(ExecutionOptions options) {
		return " WHEN MATCHED THEN ";
	}

	@Override
	protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionOptions options,
			String whereClause, ActionGeneratorChain chain) {

		StringBuilder sql = new StringBuilder("UPDATE SET ");

//		String[] pkFields = insertOrUpdateStatement.getPrimaryKey().split(",");
//		HashSet<String> hashPkFields = new HashSet<String>(Arrays.asList(pkFields));
		for (String columnKey : insertOrUpdateStatement.getColumnValues().keySet()) {
//			if (!hashPkFields.contains(columnKey)) {
				sql.append(columnKey).append(" = ");
				sql.append(convertToString(insertOrUpdateStatement.getColumnValue(columnKey), options.getRuntimeEnvironment().getTargetDatabase()));
				sql.append(",");
//			}
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
		if (newValue == null || newValue.toString().equals("") || newValue.toString().equalsIgnoreCase("NULL")) {
			sqlString = "NULL";
		} else if (newValue instanceof String && !looksLikeFunctionCall(((String) newValue), database)) {
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
