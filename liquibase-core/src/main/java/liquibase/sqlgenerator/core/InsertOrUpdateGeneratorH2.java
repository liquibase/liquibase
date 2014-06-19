package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.executor.ExecutionOptions;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertOrUpdateStatement;

import java.util.regex.Matcher;

public class InsertOrUpdateGeneratorH2 extends InsertOrUpdateGenerator {
    @Override
    public boolean supports(InsertOrUpdateStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof H2Database;
    }

    @Override
    protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        String insertStatement = super.getInsertStatement(insertOrUpdateStatement, options, sqlGeneratorChain);
        return insertStatement.replaceAll("(?i)insert into (.+) (values .+)", "MERGE INTO $1 KEY(" + Matcher.quoteReplacement(insertOrUpdateStatement.getPrimaryKey()) + ") $2");
    }

    @Override
    protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionOptions options, String whereClause, SqlGeneratorChain sqlGeneratorChain) {
        return "";
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionOptions options, String whereClause) {
        return "";
    }

    @Override
    protected String getElse(ExecutionOptions options) {
        return "";
    }

}