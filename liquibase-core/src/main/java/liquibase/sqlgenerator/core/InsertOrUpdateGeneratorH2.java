package liquibase.sqlgenerator.core;

import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.core.H2Database;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.InsertOrUpdateStatement;

import java.util.regex.Matcher;

public class InsertOrUpdateGeneratorH2 extends InsertOrUpdateGenerator {
    @Override
    public boolean supports(InsertOrUpdateStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof H2Database;
    }

    @Override
    protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionOptions options, ActionGeneratorChain chain) {
        String insertStatement = super.getInsertStatement(insertOrUpdateStatement, options, chain);
        return insertStatement.replaceAll("(?i)insert into (.+) (values .+)", "MERGE INTO $1 KEY(" + Matcher.quoteReplacement(insertOrUpdateStatement.getPrimaryKey()) + ") $2");
    }


    @Override
    protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionOptions options, String whereClause, ActionGeneratorChain chain) {
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