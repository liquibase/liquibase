package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ActionStatus;
import liquibase.action.core.*;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.datatype.DataTypeFactory;
import liquibase.snapshot.SnapshotFactory;
import liquibase.structure.ObjectName;
import liquibase.structure.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.CollectionUtil;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddColumnsLogic extends AbstractActionLogic<AddColumnsAction> {

    public static enum Clauses {
        nullable,
        primaryKey,
    }

    @Override
    protected Class<AddColumnsAction> getSupportedAction() {
        return AddColumnsAction.class;
    }

    @Override
    public ValidationErrors validate(AddColumnsAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope)
                .checkForRequiredField("columns", action);

        if (errors.hasErrors()) {
            return errors;
        }
        List<Column> columns = action.columns;

        for (Column column : columns) {
            errors.checkForRequiredField("name", column)
                    .checkForRequiredField("type", column);
        }


//        if (statement.isPrimaryKey() && (database instanceof H2Database
//                || database instanceof DB2Database
//                || database instanceof DerbyDatabase
//                || database instanceof SQLiteDatabase)) {
//            validationErrors.addError("Cannot add a primary key column");
//        }
//
//        // TODO HsqlDatabase autoincrement on non primary key? other databases?
//        if (database instanceof MySQLDatabase && statement.isAutoIncrement() && !statement.isPrimaryKey()) {
//            validationErrors.addError("Cannot add a non-primary key identity column");
//        }
//
//        // TODO is this feature valid for other databases?
//        if ((statement.getAddAfterColumn() != null) && !(database instanceof MySQLDatabase)) {
//            validationErrors.addError("Cannot add column on specific position");
//        }
//        if ((statement.getAddBeforeColumn() != null) && !((database instanceof H2Database) || (database instanceof HsqlDatabase))) {
//            validationErrors.addError("Cannot add column on specific position");
//        }
//        if ((statement.getAddAtPosition() != null) && !(database instanceof FirebirdDatabase)) {
//            validationErrors.addError("Cannot add column on specific position");
//        }

        return errors;
    }

    @Override
    public ActionStatus checkStatus(AddColumnsAction action, Scope scope) {
        ActionStatus result = new ActionStatus();
        ObjectName tableName = action.columns.get(0).name.container;

        try {
            for (Column actionColumn : action.columns) {
                Column snapshotColumn = scope.getSingleton(SnapshotFactory.class).get(actionColumn, scope);
                if (snapshotColumn == null) {
                    result.assertApplied(false, "Column '"+actionColumn.name+"' not found");
                } else {
                    Table table = scope.getSingleton(SnapshotFactory.class).get(new Table(snapshotColumn.name.container), scope);
                    if (table == null) {
                        result.unknown("Cannot find table "+snapshotColumn.name.container);
                    } else {
                        result.assertCorrect(actionColumn, snapshotColumn);
                    }
                }
            }

            if (action.primaryKey != null) {
                PrimaryKey snapshotPK = scope.getSingleton(SnapshotFactory.class).get(new PrimaryKey(new ObjectName(tableName, null)), scope);
                if (snapshotPK == null) {
                    result.assertApplied(false, "No primary key on '"+tableName+"'");
                } else {
                    for (Column actionColumn : action.columns) {
                        boolean pkHasColumn = false;
                        for (ObjectName pkColumn : snapshotPK.columns) {
                            if (pkColumn.name.equals(actionColumn.getSimpleName())) {
                                pkHasColumn = true;
                                break;
                            }
                        }
                        result.assertCorrect(pkHasColumn, "Column '"+actionColumn.name+"' is not part of the primary key");
                    }
                    result.assertCorrect(action.primaryKey, snapshotPK);
                }
            }

            for (ForeignKey actionFK : action.foreignKeys) {
                ForeignKey snapshotFK = scope.getSingleton(SnapshotFactory.class).get(actionFK, scope);
                if (snapshotFK == null) {
                    result.assertApplied(false, "Foreign Key not created on '"+tableName+"'");
                } else {
                    result.assertCorrect(actionFK, snapshotFK);
                }
            }
        } catch (Throwable e) {
            return result.unknown(e);
        }
        return result;
    }


    @Override
    public ActionResult execute(AddColumnsAction action, Scope scope) throws ActionPerformException {
        List<Action> actions = new ArrayList<>();

        for (Column column : action.columns) {
            actions.addAll(Arrays.asList(execute(column, action, scope)));
        }

        addUniqueConstraintActions(action, scope, actions);
        addForeignKeyStatements(action, scope, actions);

        return new DelegateResult(actions.toArray(new Action[actions.size()]));
    }

    protected Action[] execute(Column column, AddColumnsAction action, Scope scope) {
        List<Action> returnActions = new ArrayList<>();
        returnActions.add(new AlterTableAction(
                column.name.container,
                getColumnClause(column, action, scope)
        ));


        return returnActions.toArray(new Action[returnActions.size()]);
    }

    protected StringClauses getColumnClause(Column column, AddColumnsAction action, Scope scope) {
        StringClauses clauses = new StringClauses();
        Database database = scope.getDatabase();

        ObjectName columnName = column.name;
        DataType columnType = column.type;
        Column.AutoIncrementInformation autoIncrement = column.autoIncrementInformation;
        boolean primaryKey = false; //ObjectUtil.defaultIfEmpty(column.isPrimaryKey, false);
        boolean nullable = false; // primaryKey || ObjectUtil.defaultIfEmpty(column.nullable, false);
//        String addAfterColumn = column.addAfterColumn;

        clauses.append("ADD " + database.escapeObjectName(columnName.name, Column.class) + " " + DataTypeFactory.getInstance().fromDescription(columnType + (autoIncrement == null ? "" : "{autoIncrement:true}"), database).toDatabaseDataType(database));

        clauses.append(getDefaultValueClause(column, action, scope));

        if (autoIncrement != null && database.supportsAutoIncrement()) {
            clauses.append(database.getAutoIncrementClause(autoIncrement.startWith, autoIncrement.incrementBy));
        }

        if (nullable) {
            if (database.requiresDefiningColumnsAsNull()) {
                clauses.append(Clauses.nullable, "NULL");
            }
        } else {
            clauses.append(Clauses.nullable, "NOT NULL");
        }

        clauses.append(Clauses.primaryKey, primaryKey ? "PRIMARY KEY" : null);

//        if (database instanceof MySQLDatabase && statement.getRemarks() != null) {
//            alterTable += " COMMENT '" + statement.getRemarks() + "' ";
//        }

//        if (addAfterColumn != null) {
//            clauses.append("AFTER " + database.escapeObjectName(addAfterColumn, Column.class));
//        }

        return clauses;
    }

    protected void addUniqueConstraintActions(AddColumnsAction action, Scope scope, List<Action> returnActions) {
        List<UniqueConstraint> constraints = CollectionUtil.createIfNull(action.uniqueConstraints);
        for (UniqueConstraint constraint : constraints) {
            returnActions.add(new AddUniqueConstraintAction(constraint));
        }
    }

    protected void addForeignKeyStatements(AddColumnsAction action, Scope scope, List<Action> returnActions) {
        List<ForeignKey> constraints = CollectionUtil.createIfNull(action.foreignKeys);

        for (ForeignKey fkConstraint : constraints) {
            returnActions.add(new AddForeignKeyConstraintAction(fkConstraint));
        }
    }

    protected String getDefaultValueClause(Column column, AddColumnsAction action, Scope scope) {
        Database database = scope.getDatabase();
        Object defaultValue = column.defaultValue;
        if (defaultValue != null) {
            return "DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
        }
        return null;
    }
}
