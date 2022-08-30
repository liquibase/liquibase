package liquibase.sqlgenerator.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.column.LiquibaseColumn;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.UpdateChangeSetChecksumStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Column;

public class UpdateChangeSetChecksumGenerator extends AbstractSqlGenerator<UpdateChangeSetChecksumStatement> {
    @Override
    public ValidationErrors validate(UpdateChangeSetChecksumStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("changeSet", statement.getChangeSet());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(UpdateChangeSetChecksumStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ChangeSet changeSet = statement.getChangeSet();
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            SqlStatement runStatement = null;
            runStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                    .addNewColumnValue("MD5SUM", changeSet.generateCheckSum().toString())
                    .setWhereClause(database.escapeObjectName("ID", LiquibaseColumn.class) + " = ? " +
                            "AND " + database.escapeObjectName("AUTHOR", LiquibaseColumn.class) + " = ? " +
                            "AND " + database.escapeObjectName("FILENAME", LiquibaseColumn.class) + " = ?")
                    .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath());

            return SqlGeneratorFactory.getInstance().generateSql(runStatement, database);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }
}