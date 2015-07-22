package liquibase.actionlogic.core.db2;

import liquibase.actionlogic.core.InsertOrUpdateLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Table;

public class InsertOrUpdateLogicDB2 extends InsertOrUpdateLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }

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
        recordCheckSql.append("\tSET v_reccount = (SELECT COUNT(*) FROM " + database.escapeObjectName(new ObjectName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName()), Table.class) + " WHERE ");

        recordCheckSql.append(whereClause);
        recordCheckSql.append(");\n");

        recordCheckSql.append("\tIF v_reccount = 0 THEN\n");

        return recordCheckSql.toString();
	}
	

	@Override
	protected String getPostUpdateStatements(Database database) {
        StringBuffer endStatements = new StringBuffer();
        endStatements.append("END IF;\n");
        endStatements.append("END\n");
        return endStatements.toString();
	}

}
