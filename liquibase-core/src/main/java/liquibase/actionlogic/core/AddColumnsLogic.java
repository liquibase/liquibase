package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.*;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.datatype.DataTypeFactory;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.exception.ValidationErrors;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.CollectionUtil;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddColumnsLogic extends AbstractActionLogic<AddColumnsAction> {

    public static enum Clauses {
        nullable,
    }

    @Override
    protected Class<AddColumnsAction> getSupportedAction() {
        return AddColumnsAction.class;
    }

    @Override
    public ValidationErrors validate(AddColumnsAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope)
                .checkForRequiredField("columnDefinitions", action);

        if (errors.hasErrors()) {
            return errors;
        }
        List<ColumnDefinition> columns = action.columnDefinitions;

        for (ColumnDefinition column : columns) {
            errors.checkForRequiredField("columnName", column)
                    .checkForRequiredField("columnType", column);
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
    public ActionResult execute(AddColumnsAction action, Scope scope) throws ActionPerformException {
        List<Action> actions = new ArrayList<>();
        List<ColumnDefinition> columns = action.columnDefinitions;

        for (ColumnDefinition column : columns) {
            actions.addAll(Arrays.asList(execute(column, action, scope)));
        }

        addUniqueConstraintActions(action, scope, actions);
        addForeignKeyStatements(action, scope, actions);

        return new DelegateResult(actions.toArray(new Action[actions.size()]));
    }

    protected Action[] execute(ColumnDefinition column, AddColumnsAction action, Scope scope) {
        List<Action> returnActions = new ArrayList<>();
        returnActions.add(new AlterTableAction(
                action.tableName,
                getColumnDefinitionClauses(column, action, scope)
        ));


        return returnActions.toArray(new Action[returnActions.size()]);
    }

    protected StringClauses getColumnDefinitionClauses(ColumnDefinition column, AddColumnsAction action, Scope scope) {
        StringClauses clauses = new StringClauses();
        Database database = scope.getDatabase();

        ObjectName columnName = column.columnName;
        String columnType = column.columnType;
        AutoIncrementDefinition autoIncrement = column.autoIncrementDefinition;
        boolean primaryKey = ObjectUtil.defaultIfEmpty(column.isPrimaryKey, false);
        boolean nullable = primaryKey || ObjectUtil.defaultIfEmpty(column.isNullable, false);
        String addAfterColumn = column.addAfterColumn;

        clauses.append("ADD " + database.escapeObjectName(columnName, Column.class) + " " + DataTypeFactory.getInstance().fromDescription(columnType + (autoIncrement == null ? "" : "{autoIncrement:true}"), database).toDatabaseDataType(database));

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

        if (primaryKey) {
            clauses.append("PRIMARY KEY");
        }

//        if (database instanceof MySQLDatabase && statement.getRemarks() != null) {
//            alterTable += " COMMENT '" + statement.getRemarks() + "' ";
//        }

        if (addAfterColumn != null) {
            clauses.append("AFTER " + database.escapeObjectName(addAfterColumn, Column.class));
        }

        return clauses;
    }

    protected void addUniqueConstraintActions(AddColumnsAction action, Scope scope, List<Action> returnActions) {
        List<UniqueConstraintDefinition> constraints = CollectionUtil.createIfNull(action.uniqueConstraintDefinitions);
        for (UniqueConstraintDefinition def : constraints) {
            ObjectName tableName = action.tableName;
            List<String> columnNames = def.columnNames;
            String constraintName = def.constraintName;


            returnActions.add(new AddUniqueConstraintAction(tableName, constraintName, columnNames));
        }
    }

    protected void addForeignKeyStatements(AddColumnsAction action, Scope scope, List<Action> returnActions) {
        List<ForeignKeyDefinition> constraints = CollectionUtil.createIfNull(action.foreignKeyDefinitions);

        for (ForeignKeyDefinition fkConstraint : constraints) {
            ObjectName refTableName;
            String refColName;
            ObjectName baseTableName = action.tableName;
            List<String> baseColumnNames = fkConstraint.columnNames;


            String foreignKeyName = fkConstraint.foreignKeyName;
            StringClauses references = fkConstraint.references;

            if (references != null) {
                Matcher referencesMatcher = Pattern.compile("([\\w\\._]+)\\(([\\w_]+)\\)").matcher(references.toString());
                if (!referencesMatcher.matches()) {
                    throw new UnexpectedLiquibaseException("Don't know how to find table and column names from " +references);
                }
                refTableName = ObjectName.parse(referencesMatcher.group(1));
                refColName = referencesMatcher.group(2);
            } else {
                refTableName = fkConstraint.referencedTableName;
                refColName =  StringUtils.join(fkConstraint.referencedColumnNames, ", ");
            }

            List<String> refColumns = StringUtils.splitAndTrim(refColName, ",");
            returnActions.add(new AddForeignKeyConstraintAction(foreignKeyName, baseTableName, StringUtils.join(baseColumnNames, ", "), refTableName, StringUtils.join(refColumns, ", ")));
        }
    }

    protected String getDefaultValueClause(ColumnDefinition column, AddColumnsAction action, Scope scope) {
        Database database = scope.getDatabase();
        Object defaultValue = column.defaultValue;
        if (defaultValue != null) {
            return "DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
        }
        return null;
    }
}
