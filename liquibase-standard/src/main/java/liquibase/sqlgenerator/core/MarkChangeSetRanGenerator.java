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

                final String tag = getTagFromChangeset(changeSet);
                final int orderExecuted = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database).getNextSequenceValue();
	            final DatabaseFunction dateExecuted = new DatabaseFunction(dateValue);
                final String liquibaseVersion = getLiquibaseBuildVersion();
                final String description = StringUtil.limitSize(changeSet.getDescription(), 250);
                final String md5Sum = changeSet.generateCheckSum(ChecksumVersion.latest()).toString();
				final String execType = statement.getExecType().value;
				final String deploymentId = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database).getDeploymentId();

				if (statement.getExecType().ranBefore) {
	                runStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
	                        .addNewColumnValue("DATEEXECUTED", dateExecuted)
	                        .addNewColumnValue("ORDEREXECUTED", orderExecuted)
	                        .addNewColumnValue("MD5SUM", md5Sum)
	                        .addNewColumnValue("EXECTYPE", execType)
	                        .addNewColumnValue("DEPLOYMENT_ID", deploymentId)
	                        .addNewColumnValue(COMMENTS, getCommentsColumn(changeSet))
	                        .addNewColumnValue(CONTEXTS, getContextsColumn(changeSet))
	                        .addNewColumnValue(LABELS, getLabelsColumn(changeSet))
                            .addNewColumnValue("LIQUIBASE", liquibaseVersion)
                            .addNewColumnValue("DESCRIPTION", description)
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
                            .addColumnValue("DATEEXECUTED", dateExecuted)
                            .addColumnValue("ORDEREXECUTED", orderExecuted)
                            .addColumnValue("MD5SUM", md5Sum)
                            .addColumnValue("DESCRIPTION", description)
                            .addColumnValue(COMMENTS, getCommentsColumn(changeSet))
                            .addColumnValue("EXECTYPE", execType)
                            .addColumnValue(CONTEXTS, getContextsColumn(changeSet))
                            .addColumnValue(LABELS, getLabelsColumn(changeSet))
                            .addColumnValue("LIQUIBASE", liquibaseVersion)
                            .addColumnValue("DEPLOYMENT_ID", deploymentId);

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

    public static String getTagFromChangeset(ChangeSet changeSet) {
        if (changeSet != null) {
            for (Change change : changeSet.getChanges()) {
                if (change instanceof TagDatabaseChange) {
                    TagDatabaseChange tagChange = (TagDatabaseChange) change;
                    return tagChange.getTag();
                }
            }
        }
        return null;
    }

    public static String getLiquibaseBuildVersion() {
        return StringUtil.limitSize(LiquibaseUtil.getBuildVersion()
                .replace("SNAPSHOT", "SNP")
                .replace("beta", "b")
                .replace("alpha", "b"), 20);
    }

    private String getCommentsColumn(ChangeSet changeSet) {
        return StringUtil.limitSize(StringUtil.trimToEmpty(changeSet.getComments()), 250);
    }

    protected String getContextsColumn(ChangeSet changeSet) {
        return changeSet.buildFullContext();
    }

    protected String getLabelsColumn(ChangeSet changeSet) {
        return changeSet.buildFullLabels();
    }
}
