package liquibase.sqlgenerator.core;

import liquibase.ContextExpression;
import liquibase.change.Change;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.column.LiquibaseColumn;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtil;

public class MarkChangeSetRanGenerator extends AbstractSqlGenerator<MarkChangeSetRanStatement> {

    public static final String AND = " AND ";
    public static final String OPEN_BRACKET = "(";
    public static final String CLOSE_BRACKET = ")";
    public static final String WHITESPACE = " ";
    public static final String COMMA = ",";

    @Override
    public ValidationErrors validate(MarkChangeSetRanStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("changeSet", statement.getChangeSet());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(MarkChangeSetRanStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String dateValue = database.getCurrentDateTimeFunction();

        ChangeSet changeSet = statement.getChangeSet();

        SqlStatement runStatement;
        try {
            if (statement.getExecType().equals(ChangeSet.ExecType.FAILED) || statement.getExecType().equals(ChangeSet.ExecType.SKIPPED)) {
                return new Sql[0]; //don't mark
            }

            String tag = null;
            for (Change change : changeSet.getChanges()) {
                if (change instanceof TagDatabaseChange) {
                    TagDatabaseChange tagChange = (TagDatabaseChange) change;
                    tag = tagChange.getTag();
                }
            }

            if (statement.getExecType().ranBefore) {
                runStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                        .addNewColumnValue("DATEEXECUTED", new DatabaseFunction(dateValue))
                        .addNewColumnValue("ORDEREXECUTED", ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getNextSequenceValue())
                        .addNewColumnValue("MD5SUM", changeSet.generateCheckSum().toString())
                        .addNewColumnValue("EXECTYPE", statement.getExecType().value)
                        .addNewColumnValue("DEPLOYMENT_ID", ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getDeploymentId())
                        .setWhereClause(database.escapeObjectName("ID", LiquibaseColumn.class) + " = ? " +
                                                "AND " + database.escapeObjectName("AUTHOR", LiquibaseColumn.class) + " = ? " +
                                                "AND " + database.escapeObjectName("FILENAME", LiquibaseColumn.class) + " = ?")
                        .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath());

                if (tag != null) {
                    ((UpdateStatement) runStatement).addNewColumnValue("TAG", tag);
                }
            } else {
                runStatement = new InsertStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                        .addColumnValue("ID", changeSet.getId())
                        .addColumnValue("AUTHOR", changeSet.getAuthor())
                        .addColumnValue("FILENAME", changeSet.getFilePath())
                        .addColumnValue("DATEEXECUTED", new DatabaseFunction(dateValue))
                        .addColumnValue("ORDEREXECUTED", ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getNextSequenceValue())
                        .addColumnValue("MD5SUM", changeSet.generateCheckSum().toString())
                        .addColumnValue("DESCRIPTION", limitSize(changeSet.getDescription()))
                        .addColumnValue("COMMENTS", limitSize(StringUtil.trimToEmpty(changeSet.getComments())))
                        .addColumnValue("EXECTYPE", statement.getExecType().value)
                        .addColumnValue("CONTEXTS", ((changeSet.getContexts() == null) || changeSet.getContexts()
                            .isEmpty()) ? null : buildFullContext(changeSet))
                        .addColumnValue("LABELS", ((changeSet.getLabels() == null) || changeSet.getLabels().isEmpty()
                        ) ? null : changeSet.getLabels().toString())
                        .addColumnValue("LIQUIBASE", LiquibaseUtil.getBuildVersion().replaceAll("SNAPSHOT", "SNP"))
                        .addColumnValue("DEPLOYMENT_ID", ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getDeploymentId());

                if (tag != null) {
                    ((InsertStatement) runStatement).addColumnValue("TAG", tag);
                }
            }
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        return SqlGeneratorFactory.getInstance().generateSql(runStatement, database);
    }

    private String buildFullContext(ChangeSet changeSet) {
        StringBuilder contextExpression = new StringBuilder();
        boolean notFirstContext = false;
        for (ContextExpression inheritableContext : changeSet.getInheritableContexts()) {
            appendContext(contextExpression, inheritableContext.toString(), notFirstContext);
            notFirstContext = true;
        }
        ContextExpression changeSetContext = changeSet.getContexts();
        if ((changeSetContext != null) && !changeSetContext.isEmpty()) {
            appendContext(contextExpression, changeSetContext.toString(), notFirstContext);
        }
        return contextExpression.toString();
    }

    private void appendContext(StringBuilder contextExpression, String contextToAppend, boolean notFirstContext) {
        boolean complexExpression = contextToAppend.contains(COMMA) || contextToAppend.contains(WHITESPACE);
        if (notFirstContext) {
            contextExpression.append(AND);
        }
        if (complexExpression) {
            contextExpression.append(OPEN_BRACKET);
        }
        contextExpression.append(contextToAppend);
        if (complexExpression) {
            contextExpression.append(CLOSE_BRACKET);
        }
    }

    private String limitSize(String string) {
        int maxLength = 250;
        if (string.length() > maxLength) {
            return string.substring(0, maxLength - 3) + "...";
        }
        return string;
    }
}
