package liquibase.sqlgenerator.core;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.ComputedDateValue;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtils;

public class MarkChangeSetRanGenerator implements SqlGenerator<MarkChangeSetRanStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(MarkChangeSetRanStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(MarkChangeSetRanStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("changeSet", statement.getChangeSet());

        return validationErrors;
    }

    public Sql[] generateSql(MarkChangeSetRanStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String dateValue = database.getCurrentDateTimeFunction();

        ChangeSet changeSet = statement.getChangeSet();

        SqlStatement runStatement;
        try {
            if (statement.isRanBefore()) {
                runStatement = new UpdateStatement(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                        .addNewColumnValue("DATEEXECUTED", new ComputedDateValue(dateValue))
                        .addNewColumnValue("MD5SUM", changeSet.generateCheckSum().toString())
                        .setWhereClause("ID=? AND AUTHOR=? AND FILENAME=?")
                        .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath());
            } else {
                runStatement = new InsertStatement(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                        .addColumnValue("ID", changeSet.getId())
                        .addColumnValue("AUTHOR", changeSet.getAuthor())
                        .addColumnValue("FILENAME", changeSet.getFilePath())
                        .addColumnValue("DATEEXECUTED", new ComputedDateValue(dateValue))
                        .addColumnValue("ORDEREXECUTED", database.getNextChangeSetSequenceValue())
                        .addColumnValue("MD5SUM", changeSet.generateCheckSum().toString())
                        .addColumnValue("DESCRIPTION", limitSize(changeSet.getDescription()))
                        .addColumnValue("COMMENTS", limitSize(StringUtils.trimToEmpty(changeSet.getComments())))
                        .addColumnValue("LIQUIBASE", LiquibaseUtil.getBuildVersion());
            }
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        return SqlGeneratorFactory.getInstance().generateSql(runStatement, database);
    }

    private String limitSize(String string) {
        int maxLength = 255;
        if (string.length() > maxLength) {
            return string.substring(0, maxLength - 3) + "...";
        }
        return string;
    }
}
