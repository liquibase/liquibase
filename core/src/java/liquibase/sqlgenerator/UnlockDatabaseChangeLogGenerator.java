package liquibase.sqlgenerator;

import liquibase.statement.UnlockDatabaseChangeLogStatement;
import liquibase.statement.UpdateStatement;
import liquibase.database.Database;
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
        UpdateStatement releaseStatement = new UpdateStatement(database.getDefaultSchemaName(), database.getDatabaseChangeLogLockTableName());
        releaseStatement.addNewColumnValue("LOCKED", false);
        releaseStatement.addNewColumnValue("LOCKGRANTED", null);
        releaseStatement.addNewColumnValue("LOCKEDBY", null);
        releaseStatement.setWhereClause(" ID = 1");

        return SqlGeneratorFactory.getInstance().generateSql(releaseStatement, database);
    }
}
