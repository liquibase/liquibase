package liquibase.actionlogic.core.h2;

import liquibase.actionlogic.core.InsertOrUpdateLogic;
import liquibase.database.Database;
import liquibase.database.core.h2.H2Database;
import liquibase.statement.core.InsertOrUpdateStatement;

public class InsertOrUpdateLogicH2 extends InsertOrUpdateLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return H2Database.class;
    }

//    @Override
//    protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
//        String insertStatement = super.getInsertStatement(insertOrUpdateStatement, database, sqlGeneratorChain);
//        return insertStatement.replaceAll("(?i)insert into (.+) (values .+)", "MERGE INTO $1 KEY(" + Matcher.quoteReplacement(insertOrUpdateStatement.getPrimaryKey()) + ") $2");
//    }

//    @Override
//    protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause, SqlGeneratorChain sqlGeneratorChain) {
//        return "";
//    }

    @Override
    protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause) {
        return "";
    }

    @Override
    protected String getElse(Database database) {
        return "";
    }

}