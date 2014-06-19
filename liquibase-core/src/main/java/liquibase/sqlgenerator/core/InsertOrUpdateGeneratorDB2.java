package liquibase.sqlgenerator.core;

import liquibase.database.core.DB2Database;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.InsertOrUpdateStatement;

public class InsertOrUpdateGeneratorDB2 extends InsertOrUpdateGenerator {

	@Override
	protected String getElse(ExecutionOptions options) {
        return "\tELSEIF v_reccount = 1 THEN\n";
	}

	@Override
	protected String getRecordCheck(
			InsertOrUpdateStatement insertOrUpdateStatement, ExecutionOptions options,
			String whereClause) {
        StringBuffer recordCheckSql = new StringBuffer();

        recordCheckSql.append("BEGIN ATOMIC\n");
        recordCheckSql.append("\tDECLARE v_reccount INTEGER;\n");
        recordCheckSql.append("\tSET v_reccount = (SELECT COUNT(*) FROM " + options.getRuntimeEnvironment().getTargetDatabase().escapeTableName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName()) + " WHERE ");

        recordCheckSql.append(whereClause);
        recordCheckSql.append(");\n");

        recordCheckSql.append("\tIF v_reccount = 0 THEN\n");

        return recordCheckSql.toString();
	}
	
	@Override
	public boolean supports(InsertOrUpdateStatement statement, ExecutionOptions options) {
		return options.getRuntimeEnvironment().getTargetDatabase() instanceof DB2Database;
	}
	
	@Override
	protected String getPostUpdateStatements(ExecutionOptions options) {
        StringBuffer endStatements = new StringBuffer();
        endStatements.append("END IF;\n");
        endStatements.append("END\n");
        return endStatements.toString();
	}

}
