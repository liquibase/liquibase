package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.actiongenerator.ActionGeneratorFactory;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sqlgenerator.SqlGeneratorChain;
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
    public Action[] generateActions(RemoveChangeSetRanStatusStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        ChangeSet changeSet = statement.getChangeSet();

        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        return ActionGeneratorFactory.getInstance().generateActions(new DeleteStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .setWhereClause("ID=? AND AUTHOR=? AND FILENAME=?")
                .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath())
                , options);
    }
}
