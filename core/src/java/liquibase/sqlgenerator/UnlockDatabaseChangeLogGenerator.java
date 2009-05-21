package liquibase.sqlgenerator;

import liquibase.statement.UnlockDatabaseChangeLogStatement;
import liquibase.statement.UpdateStatement;
import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;

public class UnlockDatabaseChangeLogGenerator implements SqlGenerator<UnlockDatabaseChangeLogStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(UnlockDatabaseChangeLogStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(UnlockDatabaseChangeLogStatement statement, Database database) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(UnlockDatabaseChangeLogStatement statement, Database database) {
    	String liquibaseSchema = null;
    	try {
    		liquibaseSchema = database.getLiquibaseSchemaName();
    	} catch (JDBCException e) {
            throw new UnexpectedLiquibaseException(e);
    	}

        UpdateStatement releaseStatement = new UpdateStatement(liquibaseSchema, database.getDatabaseChangeLogLockTableName());
        releaseStatement.addNewColumnValue("LOCKED", false);
        releaseStatement.addNewColumnValue("LOCKGRANTED", null);
        releaseStatement.addNewColumnValue("LOCKEDBY", null);
        releaseStatement.setWhereClause(" ID = 1");

        return SqlGeneratorFactory.getInstance().generateSql(releaseStatement, database);
    }
}
