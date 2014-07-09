package liquibase.sqlgenerator.core;

import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.LiquibaseException;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.InsertOrUpdateDataStatement;

public class InsertOrUpdateGeneratorMSSQL extends InsertOrUpdateGenerator {
    @Override
    public boolean supports(InsertOrUpdateDataStatement statement, ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();

        return database instanceof MSSQLDatabase;
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env, String whereClause) {
        Database database = env.getTargetDatabase();

        StringBuffer recordCheck = new StringBuffer();
        recordCheck.append("DECLARE @reccount integer\n");
        recordCheck.append("SELECT @reccount = count(*) FROM ");
        recordCheck.append(database.escapeTableName(insertOrUpdateDataStatement.getCatalogName(), insertOrUpdateDataStatement.getSchemaName(), insertOrUpdateDataStatement.getTableName()));
        recordCheck.append(" WHERE ");
        recordCheck.append(whereClause);
        recordCheck.append("\n");
        recordCheck.append("IF @reccount = 0\n");

        return recordCheck.toString();
    }

    @Override
    protected String getInsertStatement(InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        StringBuffer insertBlock = new StringBuffer();
        insertBlock.append("BEGIN\n");
        insertBlock.append(super.getInsertStatement(insertOrUpdateDataStatement, env, chain));
        insertBlock.append("END\n");

        return insertBlock.toString(); 
    }

    @Override
    protected String getElse(ExecutionEnvironment env) {
        return "ELSE\n";
    }

    @Override
    protected String getUpdateStatement(InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env, String whereClause, StatementLogicChain chain) throws LiquibaseException {
        StringBuffer updateBlock = new StringBuffer();
        updateBlock.append("BEGIN\n");
        updateBlock.append(super.getUpdateStatement(insertOrUpdateDataStatement, env, whereClause, chain));
        updateBlock.append("END\n");
        return updateBlock.toString();
    }
}
