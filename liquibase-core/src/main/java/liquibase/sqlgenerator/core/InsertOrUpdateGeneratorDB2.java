package liquibase.sqlgenerator.core;

import liquibase.database.core.DB2Database;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.InsertOrUpdateStatement;

public class InsertOrUpdateGeneratorDB2 extends InsertOrUpdateGenerator {

	@Override
	protected String getElse(ExecutionEnvironment env) {
        return "\tELSEIF v_reccount = 1 THEN\n";
	}

	@Override
	protected String getRecordCheck(
			InsertOrUpdateStatement insertOrUpdateStatement, ExecutionEnvironment env,
			String whereClause) {
        StringBuffer recordCheckSql = new StringBuffer();

        recordCheckSql.append("BEGIN ATOMIC\n");
        recordCheckSql.append("\tDECLARE v_reccount INTEGER;\n");
        recordCheckSql.append("\tSET v_reccount = (SELECT COUNT(*) FROM " + env.getTargetDatabase().escapeTableName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName()) + " WHERE ");

        recordCheckSql.append(whereClause);
        recordCheckSql.append(");\n");

        recordCheckSql.append("\tIF v_reccount = 0 THEN\n");

        return recordCheckSql.toString();
	}
	
	@Override
	public boolean supports(InsertOrUpdateStatement statement, ExecutionEnvironment env) {
		return env.getTargetDatabase() instanceof DB2Database;
	}
	
	@Override
	protected String getPostUpdateStatements(ExecutionEnvironment env) {
        StringBuffer endStatements = new StringBuffer();
        endStatements.append("END IF;\n");
        endStatements.append("END\n");
        return endStatements.toString();
	}

}
