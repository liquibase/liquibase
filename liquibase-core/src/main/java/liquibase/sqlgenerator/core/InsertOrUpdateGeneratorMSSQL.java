package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertOrUpdateStatement;

public class InsertOrUpdateGeneratorMSSQL extends InsertOrUpdateGenerator {
    @Override
    public boolean supports(InsertOrUpdateStatement statement, ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        return database instanceof MSSQLDatabase;
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionOptions options, String whereClause) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        StringBuffer recordCheck = new StringBuffer();
        recordCheck.append("DECLARE @reccount integer\n");
        recordCheck.append("SELECT @reccount = count(*) FROM ");
        recordCheck.append(database.escapeTableName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(),insertOrUpdateStatement.getTableName()));
        recordCheck.append(" WHERE ");
        recordCheck.append(whereClause);
        recordCheck.append("\n");
        recordCheck.append("IF @reccount = 0\n");

        return recordCheck.toString();
    }

    @Override
    protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        StringBuffer insertBlock = new StringBuffer();
        insertBlock.append("BEGIN\n");
        insertBlock.append(super.getInsertStatement(insertOrUpdateStatement, options, sqlGeneratorChain));
        insertBlock.append("END\n");

        return insertBlock.toString(); 
    }

    @Override
    protected String getElse(ExecutionOptions options) {
        return "ELSE\n";
    }

    @Override
    protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionOptions options, String whereClause, SqlGeneratorChain sqlGeneratorChain) throws LiquibaseException {
        StringBuffer updateBlock = new StringBuffer();
        updateBlock.append("BEGIN\n");
        updateBlock.append(super.getUpdateStatement(insertOrUpdateStatement, options, whereClause, sqlGeneratorChain));
        updateBlock.append("END\n");
        return updateBlock.toString();
    }

    @Override
    public Sql[] generateSql(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return super.generateSql(insertOrUpdateStatement, options, sqlGeneratorChain);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
