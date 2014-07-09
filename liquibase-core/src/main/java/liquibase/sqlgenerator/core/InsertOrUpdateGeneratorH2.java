package liquibase.sqlgenerator.core;

import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.core.H2Database;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.InsertOrUpdateDataStatement;

import java.util.regex.Matcher;

public class InsertOrUpdateGeneratorH2 extends InsertOrUpdateGenerator {
    @Override
    public boolean supports(InsertOrUpdateDataStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof H2Database;
    }

    @Override
    protected String getInsertStatement(InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        String insertStatement = super.getInsertStatement(insertOrUpdateDataStatement, env, chain);
        return insertStatement.replaceAll("(?i)insert into (.+) (values .+)", "MERGE INTO $1 KEY(" + Matcher.quoteReplacement(insertOrUpdateDataStatement.getPrimaryKey()) + ") $2");
    }


    @Override
    protected String getUpdateStatement(InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env, String whereClause, StatementLogicChain chain) {
        return "";
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env, String whereClause) {
        return "";
    }

    @Override
    protected String getElse(ExecutionEnvironment env) {
        return "";
    }

}