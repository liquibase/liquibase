package liquibase.sqlgenerator.core;

import java.sql.Timestamp;
import java.util.Date;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.RemoveStaleLocksStatement;

public class RemoveStaleLocksGenerator extends AbstractSqlGenerator<RemoveStaleLocksStatement> {


    @Override
    public ValidationErrors validate(RemoveStaleLocksStatement statement, Database database,
                                     SqlGeneratorChain sqlGeneratorChain) {

        ValidationErrors validationErrors = new ValidationErrors();

        if ( statement.getMaxTTLInSeconds() < 1){
            validationErrors.addError("maxTTL in seconds must be > 1");
        }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RemoveStaleLocksStatement statement, Database database,
                             SqlGeneratorChain sqlGeneratorChain) {

        String liquibaseSchema = database.getLiquibaseSchemaName();
        String liquibaseCatalog = database.getLiquibaseCatalogName();

        DeleteStatement deleteStatement = new DeleteStatement(liquibaseCatalog, liquibaseSchema,
            database.getDatabaseChangeLogLockTableName());

        long now = new Date().getTime();
        long maxTTLSecondsAgo = now - statement.getMaxTTLInSeconds() * 1000;

        deleteStatement.setWhere(":name < :value AND :name IS NOT NULL");

        deleteStatement.addWhereColumnName("LOCKPROLONGED");
        deleteStatement.addWhereParameter(new Timestamp(maxTTLSecondsAgo));

        // add again for the IS NOT NULL check
        deleteStatement.addWhereColumnName("LOCKPROLONGED");

        return SqlGeneratorFactory.getInstance().generateSql(deleteStatement, database);
    }
}