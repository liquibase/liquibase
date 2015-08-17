package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ActionStatus;
import liquibase.action.core.*;
import liquibase.actionlogic.*;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.SnapshotFactory;
import liquibase.structure.ObjectName;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.util.CollectionUtil;
import liquibase.util.LiquibaseUtil;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class AddColumnsLogic extends AbstractActionLogic<AddColumnsAction> {

    public static enum Clauses {
        columnName,
        dataType,
        nullable,
        primaryKey,
        autoIncrement,
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

        Database database = scope.getDatabase();

        List<Column> columns = action.columns;

        for (Column column : columns) {
            errors.checkForRequiredField("name", column)
                    .checkForRequiredField("type", column);

            if (column.isAutoIncrement() && !database.supportsAutoIncrement()) {
                errors.addUnsupportedError("Auto-increment columns are not supported", database.getShortName());
            }

            if (column.isAutoIncrement() && column.defaultValue != null) {
                errors.addUnsupportedError("Adding a default value on an auto-increment column", database.getShortName());
            }
        }


//        if (statement.isPrimaryKey() && (database instanceof H2Database
//                || database instanceof DB2Database
//                || database instanceof DerbyDatabase
//                || database instanceof SQLiteDatabase)) {
//            validationErrors.addError("Cannot add a primary key column");
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
            PrimaryKey snapshotPK = null;

            if (action.primaryKey != null) {
                snapshotPK = scope.getSingleton(ActionExecutor.class).query(new SnapshotDatabaseObjectsAction(PrimaryKey.class, new ObjectReference(Table.class, tableName)), scope).asObject(PrimaryKey.class);
            }

            for (Column actionColumn : action.columns) {
                Column snapshotColumn = LiquibaseUtil.snapshotObject(Column.class, actionColumn.getObjectReference(), scope);
                if (snapshotColumn == null) {
                    result.assertApplied(false, "Column '"+actionColumn.name+"' not found");
                } else {
                    Table table = scope.getSingleton(SnapshotFactory.class).get(new ObjectReference(Table.class, snapshotColumn.name.container), scope);
                    if (table == null) {
                        result.unknown("Cannot find table " + snapshotColumn.name.container);
                    } else {
                        List<String> excludeFields = new ArrayList<>(Arrays.asList("type", "autoIncrementInformation", "nullable"));

                        if (actionColumn.nullable == null && snapshotColumn.isAutoIncrement() || snapshotPK != null && snapshotPK.columns.contains(snapshotColumn.name)) {
                            excludeFields.add("nullable"); //did not specify nullable, and auto-increment and/or PK usually auto-adds it but not always. Cannot check
                        }

                        result.assertCorrect(actionColumn, snapshotColumn, excludeFields);

                        result.assertCorrect(assertDataTypesCorrect(actionColumn, snapshotColumn, scope), "Data types do not match (expected "+actionColumn.type.standardType+", got "+snapshotColumn.type.standardType+")");

                        if (actionColumn.isAutoIncrement()) {
                            result.assertCorrect(snapshotColumn.isAutoIncrement(), "Column is not auto-increment");
                        }


                    }
                }
            }

            if (action.primaryKey != null) {
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
                    result.assertCorrect(action.primaryKey, snapshotPK, Arrays.asList("name"));
                }
            }

            for (ForeignKey actionFK : action.foreignKeys) {
                ForeignKey snapshotFK = scope.getSingleton(SnapshotFactory.class).get(actionFK.getObjectReference(), scope);
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

    protected boolean assertDataTypesCorrect(Column actionColumn, Column snapshotColumn, Scope scope) {
        return actionColumn.type.standardType == snapshotColumn.type.standardType;
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
        StringClauses clauses = new StringClauses(" ");
        Database database = scope.getDatabase();

        ObjectName columnName = column.name;
        DataType columnType = column.type;
        boolean primaryKey = action.primaryKey != null && action.primaryKey.containsColumn(column);
        boolean nullable = ObjectUtil.defaultIfEmpty(column.nullable, false); // primaryKey || ObjectUtil.defaultIfEmpty(column.nullable, false);
//        String addAfterColumn = column.addAfterColumn;

        clauses.append("ADD")
                .append(Clauses.columnName, database.escapeObjectName(columnName.name, Column.class))
                .append(Clauses.dataType, scope.getSingleton(DataTypeTranslatorFactory.class).getTranslator(scope).toSql(columnType, scope))
                .append(getDefaultValueClause(column, action, scope));

        if (column.autoIncrementInformation != null) {
            ActionLogic addAutoIncrementLogic = scope.getSingleton(ActionLogicFactory.class).getActionLogic(new AddAutoIncrementAction(), scope);
            if (addAutoIncrementLogic != null && addAutoIncrementLogic instanceof AddAutoIncrementLogic) {
                clauses.append(Clauses.autoIncrement, ((AddAutoIncrementLogic) addAutoIncrementLogic).generateAutoIncrementClause(column.autoIncrementInformation.startWith, column.autoIncrementInformation.incrementBy));
            } else {
                throw new UnexpectedLiquibaseException("Cannot use AddAutoIncrementLogic class "+addAutoIncrementLogic+" to build auto increment clauses");
            }
        }

        if (nullable) {
            if (database.requiresDefiningColumnsAsNull()) {
                clauses.append(Clauses.nullable, "NULL");
            }
        } else {
            clauses.append(Clauses.nullable, "NOT NULL");
        }

        clauses.append(Clauses.primaryKey, primaryKey ? "PRIMARY KEY" : null);

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
            returnActions.add(new AddForeignKeysAction(fkConstraint));
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
