package liquibase.sqlgenerator.core;

import liquibase.database.core.DB2Database;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.InsertOrUpdateDataStatement;

public class InsertOrUpdateGeneratorDB2 extends InsertOrUpdateGenerator {

	@Override
	protected String getElse(ExecutionEnvironment env) {
        return "\tELSEIF v_reccount = 1 THEN\n";
	}

	@Override
	protected String getRecordCheck(
			InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env,
			String whereClause) {
        StringBuffer recordCheckSql = new StringBuffer();

        recordCheckSql.append("BEGIN ATOMIC\n");
        recordCheckSql.append("\tDECLARE v_reccount INTEGER;\n");
        recordCheckSql.append("\tSET v_reccount = (SELECT COUNT(*) FROM " + env.getTargetDatabase().escapeTableName(insertOrUpdateDataStatement.getCatalogName(), insertOrUpdateDataStatement.getSchemaName(), insertOrUpdateDataStatement.getTableName()) + " WHERE ");

        recordCheckSql.append(whereClause);
        recordCheckSql.append(");\n");

        recordCheckSql.append("\tIF v_reccount = 0 THEN\n");

        return recordCheckSql.toString();
	}
	
	@Override
	public boolean supports(InsertOrUpdateDataStatement statement, ExecutionEnvironment env) {
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
