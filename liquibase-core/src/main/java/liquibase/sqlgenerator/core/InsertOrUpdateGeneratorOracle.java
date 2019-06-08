package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.statement.core.InsertOrUpdateStatement;

public class InsertOrUpdateGeneratorOracle extends InsertOrUpdateGenerator {


    @Override
    public boolean supports(InsertOrUpdateStatement statement, Database database) {
        return database instanceof OracleDatabase;
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause) {
        return String.format("DECLARE\n" +
                "\tv_reccount NUMBER := 0;\n" +
                "BEGIN\n" +
                "\tSELECT COUNT(*) INTO v_reccount FROM %s WHERE %s;\n" +
                "\tIF v_reccount = 0 THEN\n",
            database.escapeTableName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName()),
            whereClause
        );
    }

    @Override
    protected String getElse(Database database){
               return "\tELSIF v_reccount = 1 THEN\n";
    }

    @Override
    protected String getPostUpdateStatements(Database database){
        StringBuilder endStatements = new StringBuilder();
        endStatements.append("END IF;\n");
        endStatements.append("END;\n");

        if (ExecutorService.getInstance().getExecutor(database) instanceof LoggingExecutor) {
            endStatements.append("/\n");
        }

        return endStatements.toString();

    }
}
