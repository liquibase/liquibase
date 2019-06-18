package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.LiquibaseException;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertOrUpdateStatement;

public class InsertOrUpdateGeneratorMSSQL extends InsertOrUpdateGenerator {
    @Override
    public boolean supports(InsertOrUpdateStatement statement, Database database) {
         return database instanceof MSSQLDatabase;
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause) {
        return String.format("DECLARE @reccount integer\n" +
            "SELECT @reccount = count(*) FROM %s" +
            " WHERE %s\n" +
            "IF @reccount = 0\n",
            database.escapeTableName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName()),
            whereClause
        );
    }

    @Override
    protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return "BEGIN\n" +
            super.getInsertStatement(insertOrUpdateStatement, database, sqlGeneratorChain) +
            "END\n";
    }

    @Override
    protected String getElse(Database database) {
        return "ELSE\n";
    }

    @Override
    protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause, SqlGeneratorChain sqlGeneratorChain) throws LiquibaseException {
        return "BEGIN\n" +
            super.getUpdateStatement(insertOrUpdateStatement, database, whereClause, sqlGeneratorChain) +
            "END\n";
    }

    @Override
    public Sql[] generateSql(InsertOrUpdateStatement insertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return super.generateSql(insertOrUpdateStatement, database, sqlGeneratorChain);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
