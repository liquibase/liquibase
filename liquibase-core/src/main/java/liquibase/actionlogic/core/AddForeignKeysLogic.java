package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ActionStatus;
import liquibase.action.core.AddForeignKeysAction;
import liquibase.action.core.AlterTableAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.util.LiquibaseUtil;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddForeignKeysLogic extends AbstractActionLogic<AddForeignKeysAction> {

    public static enum Clauses {
        constraintName,
        baseColumnNames, referencedTableName, referencedColumnNames,
    }

    @Override
    protected Class<AddForeignKeysAction> getSupportedAction() {
        return AddForeignKeysAction.class;
    }

    @Override
    public ValidationErrors validate(AddForeignKeysAction action, Scope scope) {
        ValidationErrors validationErrors = super.validate(action, scope);

        Database database = scope.getDatabase();

        for (ForeignKey fk : action.foreignKeys) {
            if (!database.supportsInitiallyDeferrableColumns()) {
                validationErrors.checkForDisallowedField("initiallyDeferred", fk, database.getShortName());
                validationErrors.checkForDisallowedField("deferrable", fk, database.getShortName());
            }

            validationErrors.checkForRequiredField("columnChecks", fk);

            if (fk.name != null && fk.container != null && !supportsSeparateConstraintSchema() && !fk.container.equals(fk.columnChecks.get(0).baseColumn.container.container)) {
                validationErrors.addUnsupportedError("Specifying a different foreign key schema", database.getShortName());
            }
        }

        return validationErrors;
    }

    public boolean supportsSeparateConstraintSchema() {
        return false;
    }

    @Override
    public ActionStatus checkStatus(AddForeignKeysAction action, Scope scope) {
        ActionStatus result = new ActionStatus();
        try {
            for (ForeignKey actionFK : action.foreignKeys) {
                ForeignKey snapshotFK = LiquibaseUtil.snapshotObject(ForeignKey.class, actionFK.toReference(), scope);
                if (snapshotFK == null) {
                    result.assertApplied(false, "Foreign Key '" + actionFK.name + "' not found");
                } else {
                    result.assertCorrect(actionFK, snapshotFK);
                    if (actionFK.columnChecks.size() == snapshotFK.columnChecks.size()) {
                        for (int i=0; i <  actionFK.columnChecks.size(); i++) {
                            result.assertCorrect(actionFK.columnChecks.get(i), snapshotFK.columnChecks.get(i));
                        }
                    } else {
                        result.assertCorrect(false, "columnCheck sizes are different");
                    }
                }
            }
            return result;
        } catch (Throwable e) {
            return result.unknown(e);
        }
    }

    @Override
    public ActionResult execute(AddForeignKeysAction action, Scope scope) throws ActionPerformException {

        List<Action> actions = new ArrayList<>();

        for (ForeignKey fk : action.foreignKeys) {
            actions.addAll(Arrays.asList(execute(fk, action, scope)));
        }

        return new DelegateResult(actions.toArray(new Action[actions.size()]));
    }

    protected Action execute(ForeignKey fk, AddForeignKeysAction action, Scope scope) {
        return new AlterTableAction(
                fk.columnChecks.get(0).baseColumn.container,
                generateSql(fk, action, scope)
        );
    }


    protected StringClauses generateSql(ForeignKey foreignKey, AddForeignKeysAction action, Scope scope) {
        final Database database = scope.getDatabase();

        String constrantName = supportsSeparateConstraintSchema() ? database.escapeObjectName(foreignKey.name, ForeignKey.class) : database.escapeObjectName(foreignKey.getName(), ForeignKey.class);

        StringClauses clauses = new StringClauses()
                .append("ADD CONSTRAINT")
                .append(AddForeignKeysLogic.Clauses.constraintName, constrantName)
                .append("FOREIGN KEY")
                .append(Clauses.baseColumnNames, "(" + StringUtils.join(foreignKey.columnChecks, ", ", new StringUtils.StringUtilsFormatter<ForeignKey.ForeignKeyColumnCheck>() {
                    @Override
                    public String toString(ForeignKey.ForeignKeyColumnCheck obj) {
                        return database.escapeObjectName(obj.baseColumn.name, Column.class);
                    }
                }) + ")")
                .append("REFERENCES")
                .append(Clauses.referencedTableName, database.escapeObjectName(foreignKey.columnChecks.get(0).referencedColumn.container))
                .append(Clauses.referencedColumnNames, "(" + StringUtils.join(foreignKey.columnChecks, ", ", new StringUtils.StringUtilsFormatter<ForeignKey.ForeignKeyColumnCheck>() {
                    @Override
                    public String toString(ForeignKey.ForeignKeyColumnCheck obj) {
                        return database.escapeObjectName(obj.referencedColumn.name, Column.class);
                    }
                }) + ")");

        if (foreignKey.deleteRule != null) {
            String option;
            switch (foreignKey.deleteRule) {
                case importedKeyCascade: option = "CASCADE"; break;
                case importedKeyNoAction: option = "NO ACTION"; break;
                case importedKeyRestrict: option = "RESTRICT"; break;
                case importedKeySetDefault: option = "SET DEFAULT"; break;
                case importedKeySetNull: option = "SET NULL"; break;
                default: throw new UnexpectedLiquibaseException("Unknown updateRule: "+foreignKey.updateRule);
            }
            clauses.append("ON DELETE", new StringClauses(" ").append("ON DELETE").append(option));
        }

        if (foreignKey.updateRule != null) {
            String option;
            switch (foreignKey.updateRule) {
                case importedKeyCascade: option = "CASCADE"; break;
                case importedKeySetNull: option = "SET NULL"; break;
                case importedKeySetDefault: option = "SET DEFAULT"; break;
                case importedKeyRestrict: option = "RESTRICT"; break;
                case importedKeyNoAction: option = "NO ACTION"; break;
                default: throw new UnexpectedLiquibaseException("Unknown updateRule: "+foreignKey.updateRule);
            }
            clauses.append("ON UPDATE", new StringClauses(" ").append("ON UPDATE").append(option));
        }

        boolean deferrable = ObjectUtil.defaultIfEmpty(foreignKey.deferrable, false);
        boolean initiallyDeferred = ObjectUtil.defaultIfEmpty(foreignKey.initiallyDeferred, false);
        if (deferrable || initiallyDeferred) {
            if (deferrable) {
                clauses.append("DEFERRABLE");
            }

            if (initiallyDeferred) {
                clauses.append("INITIALLY DEFERRED");
            }
        }

        return clauses;
    }
}
