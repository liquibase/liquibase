package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.UnlockDatabaseChangeLogStatement;
import liquibase.statement.core.UpdateStatement;

public class UnlockDatabaseChangeLogGenerator extends AbstractSqlGenerator<UnlockDatabaseChangeLogStatement> {

    @Override
    public ValidationErrors validate(UnlockDatabaseChangeLogStatement statement, Database database,
                                     SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(UnlockDatabaseChangeLogStatement statement, Database database,
                             SqlGeneratorChain sqlGeneratorChain) {
        String liquibaseSchema = database.getLiquibaseSchemaName();

        UpdateStatement releaseStatement = new UpdateStatement(database.getLiquibaseCatalogName()
            , liquibaseSchema, database
            .getDatabaseChangeLogLockTableName());

        releaseStatement.addNewColumnValue("LOCKED", false);
        releaseStatement.addNewColumnValue("LOCKGRANTED", null);
        releaseStatement.addNewColumnValue("LOCKEXPIRES", null);
        releaseStatement.addNewColumnValue("LOCKEDBY", null);
        releaseStatement.addNewColumnValue("LOCKEDBYID", null);

        String idColumnName = database.escapeColumnName(
            database.getLiquibaseCatalogName(),
            liquibaseSchema,
            database.getDatabaseChangeLogTableName(),
            "ID");

        releaseStatement.setWhereClause(idColumnName + " = 1 AND " +
            // make sure we are only removing our own lock
            // (in case its the standard lock service, this is NULL)
            "LOCKEDBYID " + lockedByIdCheck(statement.getLockedById()));

        return SqlGeneratorFactory.getInstance().generateSql(releaseStatement, database);
    }

    private String lockedByIdCheck(String lockedById) {
        if (lockedById == null) {
            return " IS NULL ";
        }

        return " = '" + lockedById + "' ";
    }
}
