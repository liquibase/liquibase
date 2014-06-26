package liquibase.sqlgenerator.core;

import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.core.H2Database;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.InsertOrUpdateStatement;

import java.util.regex.Matcher;

public class InsertOrUpdateGeneratorH2 extends InsertOrUpdateGenerator {
    @Override
    public boolean supports(InsertOrUpdateStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof H2Database;
    }

    @Override
    protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        String insertStatement = super.getInsertStatement(insertOrUpdateStatement, env, chain);
        return insertStatement.replaceAll("(?i)insert into (.+) (values .+)", "MERGE INTO $1 KEY(" + Matcher.quoteReplacement(insertOrUpdateStatement.getPrimaryKey()) + ") $2");
    }


    @Override
    protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionEnvironment env, String whereClause, StatementLogicChain chain) {
        return "";
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionEnvironment env, String whereClause) {
        return "";
    }

    @Override
    protected String getElse(ExecutionEnvironment env) {
        return "";
    }

}