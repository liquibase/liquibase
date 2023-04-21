package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertOrUpdateStatement;

/**
 * Adds an ON CONFLICT REPLACE clause to an INSERT INTO ... statement for SQLite.
 */
public class InsertOrUpdateGeneratorSQLite extends InsertOrUpdateGenerator {
    @Override
    public boolean supports(InsertOrUpdateStatement statement, Database database) {
        return database instanceof SQLiteDatabase;
    }

    @Override
    protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String insertStatement = super.getInsertStatement(insertOrUpdateStatement, database, sqlGeneratorChain);
        return insertStatement.replaceFirst("^(?i)INSERT INTO", "INSERT OR REPLACE INTO");
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
