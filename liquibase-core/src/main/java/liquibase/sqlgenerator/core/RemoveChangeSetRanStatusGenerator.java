package liquibase.sqlgenerator.core;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.RemoveChangeSetRanStatusStatement;

public class RemoveChangeSetRanStatusGenerator extends AbstractSqlGenerator<RemoveChangeSetRanStatusStatement> {

    @Override
    public ValidationErrors validate(RemoveChangeSetRanStatusStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        errors.checkRequiredField("changeSet", statement.getChangeSet());
        return errors;
    }

    @Override
    public Sql[] generateSql(RemoveChangeSetRanStatusStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ChangeSet changeSet = statement.getChangeSet();

        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        return SqlGeneratorFactory.getInstance().generateSql(new DeleteStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .setWhereClause("ID=? AND AUTHOR=? AND FILENAME=?")
                .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath())
                , options);
    }
}
