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
import liquibase.statement.core.UpdateChangeSetFilenameStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.StringUtil;

public class UpdateChangeSetFilenameGenerator extends AbstractSqlGenerator<UpdateChangeSetFilenameStatement> {
    @Override
    public ValidationErrors validate(UpdateChangeSetFilenameStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("changeSet", statement.getChangeSet());
        validationErrors.checkRequiredField("oldFilename", statement.getOldFilename());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(UpdateChangeSetFilenameStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ChangeSet changeSet = statement.getChangeSet();
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            SqlStatement runStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                    .addNewColumnValue("FILENAME", this.getFilePath(changeSet))
                    .setWhereClause(database.escapeObjectName("ID", LiquibaseColumn.class) + " = ? " +
                            "AND " + database.escapeObjectName("AUTHOR", LiquibaseColumn.class) + " = ? " +
                            "AND " + database.escapeObjectName("FILENAME", LiquibaseColumn.class) + " = ?")
                    .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), statement.getOldFilename());

            return SqlGeneratorFactory.getInstance().generateSql(runStatement, database);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }

    private String getFilePath(ChangeSet changeSet) {
        if (StringUtil.isNotEmpty(changeSet.getStoredFilePath())) {
            return changeSet.getStoredFilePath();
        }
        return changeSet.getFilePath();
    }
}
