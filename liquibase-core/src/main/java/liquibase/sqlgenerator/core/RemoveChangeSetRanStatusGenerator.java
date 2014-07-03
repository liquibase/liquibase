package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.exception.UnsupportedException;
import liquibase.statement.core.DeleteDataStatement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.RemoveChangeSetRanStatusStatement;

public class RemoveChangeSetRanStatusGenerator extends AbstractSqlGenerator<RemoveChangeSetRanStatusStatement> {

    @Override
    public ValidationErrors validate(RemoveChangeSetRanStatusStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors errors = new ValidationErrors();
        errors.checkRequiredField("changeSet", statement.getChangeSet());
        return errors;
    }

    @Override
    public Action[] generateActions(RemoveChangeSetRanStatusStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        ChangeSet changeSet = statement.getChangeSet();

        Database database = env.getTargetDatabase();
        return StatementLogicFactory.getInstance().generateActions(new DeleteDataStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .setWhere("ID=? AND AUTHOR=? AND FILENAME=?")
                .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath())
                , env);
    }
}
