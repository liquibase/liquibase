package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.statement.core.InsertOrUpdateStatement;

public class InsertOrUpdateGeneratorDB2 extends InsertOrUpdateGenerator {

	@Override
	protected String getElse(Database database) {
        return "\tELSEIF v_reccount = 1 THEN\n";
	}

	@Override
	protected String getRecordCheck(
			InsertOrUpdateStatement insertOrUpdateStatement, Database database,
			String whereClause) {
        StringBuffer recordCheckSql = new StringBuffer();

        recordCheckSql.append("BEGIN ATOMIC\n");
        recordCheckSql.append("\tDECLARE v_reccount INTEGER;\n");
        recordCheckSql.append("\tSET v_reccount = (SELECT COUNT(*) FROM " + database.escapeTableName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName()) + " WHERE ");

        recordCheckSql.append(whereClause);
        recordCheckSql.append(");\n");

        recordCheckSql.append("\tIF v_reccount = 0 THEN\n");

        return recordCheckSql.toString();
	}
	
	@Override
	public boolean supports(InsertOrUpdateStatement statement, Database database) {
		return database instanceof DB2Database;
	}
	
	@Override
	protected String getPostUpdateStatements(Database database) {
        StringBuffer endStatements = new StringBuffer();
        endStatements.append("END IF;\n");
        endStatements.append("END\n");
        return endStatements.toString();
	}

}
