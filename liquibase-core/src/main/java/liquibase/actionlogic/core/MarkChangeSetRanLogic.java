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
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Column;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtils;

import java.util.List;

public class MarkChangeSetRanLogic extends AbstractActionLogic {


    @Override
    protected Class<? extends Action> getSupportedAction() {
        return MarkChangeSetRanAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(MarkChangeSetRanAction.Attr.changeSet, action)
                .checkForRequiredField(MarkChangeSetRanAction.Attr.execType, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        String dateValue = database.getCurrentDateTimeFunction();
        ChangeSet changeSet = action.get(MarkChangeSetRanAction.Attr.changeSet, ChangeSet.class);

        ChangeSet.ExecType execType = ChangeSet.ExecType.valueOf(action.get(MarkChangeSetRanAction.Attr.execType, String.class));

        Action runAction;
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
                        .addNewColumnValue("MD5SUM", changeSet.generateCheckSum().toString())
                        .addNewColumnValue("EXECTYPE", statement.getExecType().value)
                        .setWhereClause(database.escapeObjectName("ID", Column.class) + " = ? " +
                                "AND " + database.escapeObjectName("AUTHOR", Column.class) + " = ? " +
                                "AND " + database.escapeObjectName("FILENAME", Column.class) + " = ?")
                        .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath());

                if (tag != null) {
                    ((UpdateStatement) runStatement).addNewColumnValue("TAG", tag);
                }
            } else {
                runAction = new InsertDataAction(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                        .addColumnValue("ID", changeSet.getId())
                        .addColumnValue("AUTHOR", changeSet.getAuthor())
                        .addColumnValue("FILENAME", changeSet.getFilePath())
                        .addColumnValue("DATEEXECUTED", new DatabaseFunction(dateValue))
                        .addColumnValue("ORDEREXECUTED", ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getNextSequenceValue())
                        .addColumnValue("MD5SUM", changeSet.generateCheckSum().toString())
                        .addColumnValue("DESCRIPTION", limitSize(changeSet.getDescription()))
                        .addColumnValue("COMMENTS", limitSize(StringUtils.trimToEmpty(changeSet.getComments())))
                        .addColumnValue("EXECTYPE", statement.getExecType().value)
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
