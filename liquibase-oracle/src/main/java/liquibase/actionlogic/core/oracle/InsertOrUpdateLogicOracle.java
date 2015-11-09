package liquibase.actionlogic.core.oracle;

import liquibase.actionlogic.core.InsertOrUpdateLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Table;

public class InsertOrUpdateLogicOracle extends InsertOrUpdateLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause) {

        StringBuffer recordCheckSql = new StringBuffer();

        recordCheckSql.append("DECLARE\n");
        recordCheckSql.append("\tv_reccount NUMBER := 0;\n");
        recordCheckSql.append("BEGIN\n");
        recordCheckSql.append("\tSELECT COUNT(*) INTO v_reccount FROM " + database.escapeObjectName(new ObjectReference(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName()), Table.class) + " WHERE ");

        recordCheckSql.append(whereClause);
        recordCheckSql.append(";\n");

        recordCheckSql.append("\tIF v_reccount = 0 THEN\n");

        return recordCheckSql.toString();
    }

    @Override
    protected String getElse(Database database){
               return "\tELSIF v_reccount = 1 THEN\n";
    }

    @Override
    protected String getPostUpdateStatements(Database database){
        StringBuffer endStatements = new StringBuffer();
        endStatements.append("END IF;\n");
        endStatements.append("END;\n");

        if (ExecutorService.getInstance().getExecutor(database) instanceof LoggingExecutor) {
            endStatements.append("/\n");
        }

        return endStatements.toString();

    }
}
