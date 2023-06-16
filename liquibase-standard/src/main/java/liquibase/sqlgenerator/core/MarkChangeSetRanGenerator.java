package liquibase.sqlgenerator.core;

import liquibase.ChecksumVersion;
import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.column.LiquibaseColumn;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
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

    private static final String COMMENTS = "COMMENTS";
    private static final String CONTEXTS = "CONTEXTS";
    private static final String LABELS = "LABELS";

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
        // use LEGACY quoting since we're dealing with system objects
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            try {
                if (statement.getExecType().equals(ChangeSet.ExecType.FAILED) || statement.getExecType().equals(ChangeSet.ExecType.SKIPPED)) {
                    return EMPTY_SQL; //don't mark
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
                        .addNewColumnValue("ORDEREXECUTED", Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database).getNextSequenceValue())
                        .addNewColumnValue("MD5SUM", changeSet.generateCheckSum(ChecksumVersion.latest()).toString())
                        .addNewColumnValue("EXECTYPE", statement.getExecType().value)
                        .addNewColumnValue("DEPLOYMENT_ID", Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database).getDeploymentId())
                        .addNewColumnValue(COMMENTS, getCommentsColumn(changeSet))
                        .addNewColumnValue(CONTEXTS, getContextsColumn(changeSet))
                        .addNewColumnValue(LABELS, getLabelsColumn(changeSet))
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
                            .addColumnValue("ORDEREXECUTED", Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database).getNextSequenceValue())
                            .addColumnValue("MD5SUM", changeSet.generateCheckSum(ChecksumVersion.latest()).toString())
                            .addColumnValue("DESCRIPTION", limitSize(changeSet.getDescription()))
                            .addColumnValue(COMMENTS, getCommentsColumn(changeSet))
                            .addColumnValue("EXECTYPE", statement.getExecType().value)
                            .addColumnValue(CONTEXTS, getContextsColumn(changeSet))
                            .addColumnValue(LABELS, getLabelsColumn(changeSet))
                        .addColumnValue("LIQUIBASE", StringUtil.limitSize(LiquibaseUtil.getBuildVersion()
                                                                                            .replaceAll("SNAPSHOT", "SNP")
                                                                                            .replaceAll("beta", "b")
                                                                                            .replaceAll("alpha", "b"), 20)
                            )
                            .addColumnValue("DEPLOYMENT_ID", Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database).getDeploymentId());

                    if (tag != null) {
                        ((InsertStatement) runStatement).addColumnValue("TAG", tag);
                    }
                }
            } catch (LiquibaseException e) {
                throw new UnexpectedLiquibaseException(e);
            }

            return SqlGeneratorFactory.getInstance().generateSql(runStatement, database);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }

    private String getCommentsColumn(ChangeSet changeSet) {
        return limitSize(StringUtil.trimToEmpty(changeSet.getComments()));
    }

    protected String getContextsColumn(ChangeSet changeSet) {
        return changeSet.buildFullContext();
    }

    protected String getLabelsColumn(ChangeSet changeSet) {
        return changeSet.buildFullLabels();
    }

    private String limitSize(String string) {
        int maxLength = 250;
        if (string.length() > maxLength) {
            return string.substring(0, maxLength - 3) + "...";
        }
        return string;
    }
}
