package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.statement.Statement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.UpdateChangeSetChecksumStatement;
import liquibase.statement.core.UpdateStatement;

public class UpdateChangeSetChecksumGenerator extends AbstractSqlGenerator<UpdateChangeSetChecksumStatement> {
    @Override
    public ValidationErrors validate(UpdateChangeSetChecksumStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("changeSet", statement.getChangeSet());

        return validationErrors;
    }

    @Override
    public Action[] generateActions(UpdateChangeSetChecksumStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        ChangeSet changeSet = statement.getChangeSet();
        Database database = env.getTargetDatabase();

        Statement runStatement = null;
        runStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .addNewColumnValue("MD5SUM", changeSet.generateCheckSum().toString())
                .setWhereClause("ID=? AND AUTHOR=? AND FILENAME=?")
                .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath());

        return StatementLogicFactory.getInstance().generateActions(runStatement, env);
    }
}