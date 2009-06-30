package liquibase.sqlgenerator.core;

import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.ComputedDateValue;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.util.StringUtils;
import liquibase.util.LiquibaseUtil;
import liquibase.changelog.ChangeSet;

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

        InsertStatement insertStatement;
        try {
            insertStatement = new InsertStatement(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());
            insertStatement.addColumnValue("ID", database.escapeStringForDatabase(changeSet.getId()));
            insertStatement.addColumnValue("AUTHOR", changeSet.getAuthor());
            insertStatement.addColumnValue("FILENAME", changeSet.getFilePath());
            insertStatement.addColumnValue("DATEEXECUTED", new ComputedDateValue(dateValue));
            insertStatement.addColumnValue("ORDEREXECUTED", database.getNextChangeSetSequenceValue());
            insertStatement.addColumnValue("MD5SUM", changeSet.generateCheckSum().toString());
            insertStatement.addColumnValue("DESCRIPTION", limitSize(changeSet.getDescription()));
            insertStatement.addColumnValue("COMMENTS", limitSize(StringUtils.trimToEmpty(changeSet.getComments())));
            insertStatement.addColumnValue("LIQUIBASE", LiquibaseUtil.getBuildVersion());
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        return SqlGeneratorFactory.getInstance().generateSql(insertStatement, database);
    }
    
    private String limitSize(String string) {
        int maxLength = 255;
        if (string.length() > maxLength) {
            return string.substring(0, maxLength - 3) + "...";
        }
        return string;
    }
}
