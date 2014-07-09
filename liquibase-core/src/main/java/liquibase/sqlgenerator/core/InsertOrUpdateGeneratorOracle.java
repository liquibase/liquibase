package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import  liquibase.ExecutionEnvironment;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.statement.core.InsertOrUpdateDataStatement;

public class InsertOrUpdateGeneratorOracle extends InsertOrUpdateGenerator {


    @Override
    public boolean supports(InsertOrUpdateDataStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof OracleDatabase;
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env, String whereClause) {

        StringBuffer recordCheckSql = new StringBuffer();

        recordCheckSql.append("DECLARE\n");
        recordCheckSql.append("\tv_reccount NUMBER := 0;\n");
        recordCheckSql.append("BEGIN\n");
        Database database = env.getTargetDatabase();
        recordCheckSql.append("\tSELECT COUNT(*) INTO v_reccount FROM " + database.escapeTableName(insertOrUpdateDataStatement.getCatalogName(), insertOrUpdateDataStatement.getSchemaName(), insertOrUpdateDataStatement.getTableName()) + " WHERE ");

        recordCheckSql.append(whereClause);
        recordCheckSql.append(";\n");

        recordCheckSql.append("\tIF v_reccount = 0 THEN\n");

        return recordCheckSql.toString();
    }

    @Override
    protected String getElse(ExecutionEnvironment env){
               return "\tELSIF v_reccount = 1 THEN\n";
    }

    @Override
    protected String getPostUpdateStatements(ExecutionEnvironment env){
        StringBuffer endStatements = new StringBuffer();
        endStatements.append("END IF;\n");
        endStatements.append("END;\n");

        if (ExecutorService.getInstance().getExecutor(env.getTargetDatabase()) instanceof LoggingExecutor) {
            endStatements.append("/\n");
        }

        return endStatements.toString();

    }
}
