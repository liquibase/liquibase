package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.InsertDataAction;
import liquibase.action.core.MarkChangeSetRanAction;
import liquibase.action.core.UpdateDataAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.NoOpResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.change.Change;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.ObjectName;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

public class MarkChangeSetRanLogic extends AbstractActionLogic<MarkChangeSetRanAction> {


    @Override
    protected Class<MarkChangeSetRanAction> getSupportedAction() {
        return MarkChangeSetRanAction.class;
    }

    @Override
    public ValidationErrors validate(MarkChangeSetRanAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("changeSet", action)
                .checkForRequiredField("execType", action);
    }

    @Override
    public ActionResult execute(MarkChangeSetRanAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        String dateValue = database.getCurrentDateTimeFunction();
        ChangeSet changeSet = action.changeSet;

        ChangeSet.ExecType execType = action.execType;

        Action runAction;
        try {
            if (execType.equals(ChangeSet.ExecType.FAILED) || execType.equals(ChangeSet.ExecType.SKIPPED)) {
                return new NoOpResult(); //don't mark
            }

            String tag = null;
            for (Change change : changeSet.getChanges()) {
                if (change instanceof TagDatabaseChange) {
                    TagDatabaseChange tagChange = (TagDatabaseChange) change;
                    tag = tagChange.getTag();
                }
            }

            ObjectName tableName = new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());

            if (execType.ranBefore) {
                runAction = new UpdateDataAction(tableName)
                        .addNewColumnValue("DATEEXECUTED", new DatabaseFunction(dateValue))
                        .addNewColumnValue("MD5SUM", changeSet.generateCheckSum().toString())
                        .addNewColumnValue("EXECTYPE", execType.value)
                        .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath());
                ((UpdateDataAction) runAction).whereClause = new StringClauses().append("ID=? AND AUTHOR=? AND FILENAME=?");

                if (tag != null) {
                    ((UpdateDataAction) runAction).addNewColumnValue("TAG", tag);
                }
            } else {
                runAction = new InsertDataAction(tableName)
                        .addColumnValue("ID", changeSet.getId())
                        .addColumnValue("AUTHOR", changeSet.getAuthor())
                        .addColumnValue("FILENAME", changeSet.getFilePath())
                        .addColumnValue("DATEEXECUTED", new DatabaseFunction(dateValue))
                        .addColumnValue("ORDEREXECUTED", ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getNextSequenceValue())
                        .addColumnValue("MD5SUM", changeSet.generateCheckSum().toString())
                        .addColumnValue("DESCRIPTION", limitSize(changeSet.getDescription()))
                        .addColumnValue("COMMENTS", limitSize(StringUtils.trimToEmpty(changeSet.getComments())))
                        .addColumnValue("EXECTYPE", execType.value)
                        .addColumnValue("CONTEXTS", changeSet.getContexts() == null || changeSet.getContexts().isEmpty()? null : changeSet.getContexts().toString())
                        .addColumnValue("LABELS", changeSet.getLabels() == null || changeSet.getLabels().isEmpty() ? null : changeSet.getLabels().toString())
                        .addColumnValue("LIQUIBASE", LiquibaseUtil.getBuildVersion().replaceAll("SNAPSHOT", "SNP"));

                if (tag != null) {
                    ((InsertDataAction) runAction).addColumnValue("TAG", tag);
                }
            }
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        return new DelegateResult(runAction);
    }

    private String limitSize(String string) {
        int maxLength = 250;
        if (string.length() > maxLength) {
            return string.substring(0, maxLength - 3) + "...";
        }
        return string;
    }
}
