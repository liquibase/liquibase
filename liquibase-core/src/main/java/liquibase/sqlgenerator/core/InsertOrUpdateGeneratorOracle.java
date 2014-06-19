package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.statement.core.InsertOrUpdateStatement;

public class InsertOrUpdateGeneratorOracle extends InsertOrUpdateGenerator {


    @Override
    public boolean supports(InsertOrUpdateStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof OracleDatabase;
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionOptions options, String whereClause) {

        StringBuffer recordCheckSql = new StringBuffer();

        recordCheckSql.append("DECLARE\n");
        recordCheckSql.append("\tv_reccount NUMBER := 0;\n");
        recordCheckSql.append("BEGIN\n");
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        recordCheckSql.append("\tSELECT COUNT(*) INTO v_reccount FROM " + database.escapeTableName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName()) + " WHERE ");

        recordCheckSql.append(whereClause);
        recordCheckSql.append(";\n");

        recordCheckSql.append("\tIF v_reccount = 0 THEN\n");

        return recordCheckSql.toString();
    }

    @Override
    protected String getElse(ExecutionOptions options){
               return "\tELSIF v_reccount = 1 THEN\n";
    }

    @Override
    protected String getPostUpdateStatements(ExecutionOptions options){
        StringBuffer endStatements = new StringBuffer();
        endStatements.append("END IF;\n");
        endStatements.append("END;\n");

        if (ExecutorService.getInstance().getExecutor(options.getRuntimeEnvironment().getTargetDatabase()) instanceof LoggingExecutor) {
            endStatements.append("/\n");
        }

        return endStatements.toString();

    }
}
