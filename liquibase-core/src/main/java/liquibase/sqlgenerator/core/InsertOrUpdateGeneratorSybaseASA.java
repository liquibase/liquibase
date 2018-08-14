package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SybaseASADatabase;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertOrUpdateStatement;

/**
 *
 * @author A. Pohl
 */
public class InsertOrUpdateGeneratorSybaseASA extends InsertOrUpdateGenerator {
    @Override
    public boolean supports(InsertOrUpdateStatement statement, Database database) {
         return database instanceof SybaseASADatabase;
    }

    @Override
    protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer sql = new StringBuffer(super.getInsertStatement(insertOrUpdateStatement, database, sqlGeneratorChain));
        
        int pos = sql.indexOf("VALUES (", 0);
        sql.insert(pos, " ON EXISTING UPDATE ");

        return sql.toString();
    }

    @Override
    protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause, SqlGeneratorChain sqlGeneratorChain) {
        return "";
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause) {
        return "";
    }

    @Override
    protected String getElse(Database database) {
        return "";
    }
}
