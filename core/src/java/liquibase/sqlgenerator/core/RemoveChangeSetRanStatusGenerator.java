package liquibase.sqlgenerator.core;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.RemoveChangeSetRanStatusStatement;

public class RemoveChangeSetRanStatusGenerator implements SqlGenerator<RemoveChangeSetRanStatusStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(RemoveChangeSetRanStatusStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(RemoveChangeSetRanStatusStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        errors.checkRequiredField("changeSet", statement.getChangeSet());
        return errors;
    }

    public Sql[] generateSql(RemoveChangeSetRanStatusStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ChangeSet changeSet = statement.getChangeSet();

        return SqlGeneratorFactory.getInstance().generateSql(new DeleteStatement(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .setWhereClause("ID=? AND AUTHOR=? AND FILENAME=?")
                .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath())
                , database);
    }
}
