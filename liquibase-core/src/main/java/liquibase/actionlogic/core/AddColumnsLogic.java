package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.*;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.datatype.DataTypeFactory;
import liquibase.structure.core.Column;
import liquibase.exception.ValidationErrors;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.AddForeignKeyConstraintStatement;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddColumnsLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AddColumnsAction.class;
    }

    @Override
    protected int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope)
                .checkForRequiredField(AddColumnsAction.Attr.columnDefinitions, action);

        if (errors.hasErrors()) {
            return errors;
        }
        List<ColumnDefinition> columns = action.get(AddColumnsAction.Attr.columnDefinitions, List.class);

        for (ColumnDefinition column : columns) {
            errors.checkForRequiredField(ColumnDefinition.Attr.columnName, column)
                    .checkForRequiredField(ColumnDefinition.Attr.columnType, column);
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
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        List<Action> actions = new ArrayList<>();
        List<ColumnDefinition> columns = action.get(AddColumnsAction.Attr.columnDefinitions, List.class);

        for (ColumnDefinition column : columns) {
            actions.addAll(Arrays.asList(execute(column, action, scope)));
        }

        addUniqueConstraintActions(action, scope, actions);
        addForeignKeyStatements(action, scope, actions);

        return new RewriteResult(actions.toArray(new Action[actions.size()]));
    }

    protected Action[] execute(ColumnDefinition column, Action action, Scope scope) {
        List<Action> returnActions = new ArrayList<>();
        returnActions.add(new AlterTableAction(
                action.get(AddColumnsAction.Attr.catalogName, String.class),
                action.get(AddColumnsAction.Attr.schemaName, String.class),
                action.get(AddColumnsAction.Attr.tableName, String.class),
                StringUtils.join(getColumnDefinitionClauses(column, action, scope), " ")
        ));


        return returnActions.toArray(new Action[returnActions.size()]);
    }

    protected List<String> getColumnDefinitionClauses(ColumnDefinition column, Action action, Scope scope) {
        List<String> clauses = new ArrayList<>();
        Database database = scope.get(Scope.Attr.database, Database.class);

        String columnName = column.get(ColumnDefinition.Attr.columnName, String.class);
        String columnType = column.get(ColumnDefinition.Attr.columnType, String.class);
        AutoIncrementDefinition autoIncrement = column.get(ColumnDefinition.Attr.autoIncrementDefinition, AutoIncrementDefinition.class);
        boolean primaryKey = column.get(ColumnDefinition.Attr.isPrimaryKey, false);
        boolean nullable = primaryKey || column.get(ColumnDefinition.Attr.isNullable, false);
        String addAfterColumn = column.get(ColumnDefinition.Attr.addAfterColumn, String.class);

        clauses.add("ADD " + database.escapeObjectName(columnName, Column.class) + " " + DataTypeFactory.getInstance().fromDescription(columnType + (autoIncrement == null? "" : "{autoIncrement:true}"), database).toDatabaseDataType(database));

        clauses.add(getDefaultValueClause(column, action, scope));

        if (autoIncrement != null && database.supportsAutoIncrement()) {
            clauses.add(database.getAutoIncrementClause(autoIncrement.get(AutoIncrementDefinition.Attr.startWith, BigInteger.class), autoIncrement.get(AutoIncrementDefinition.Attr.incrementBy, BigInteger.class)));
        }

        if (nullable) {
            if (database.supportsDefiningColumnsAsNull()) {
                clauses.add("NULL");
            }
        } else {
            clauses.add("NOT NULL");
        }

        if (primaryKey) {
            clauses.add("PRIMARY KEY");
        }

//        if (database instanceof MySQLDatabase && statement.getRemarks() != null) {
//            alterTable += " COMMENT '" + statement.getRemarks() + "' ";
//        }

        if (addAfterColumn != null) {
            clauses.add("AFTER " + database.escapeObjectName(addAfterColumn, Column.class));
        }

        return clauses;
    }

    protected void addUniqueConstraintActions(Action action, Scope scope, List<Action> returnActions) {
        UniqueConstraintDefinition[] constraints = action.get(AddColumnsAction.Attr.uniqueConstraintDefinitions, new UniqueConstraintDefinition[0]);
        for (UniqueConstraintDefinition def : constraints) {
            String catalogName = action.get(AddColumnsAction.Attr.catalogName, String.class);
            String schemaName = action.get(AddColumnsAction.Attr.schemaName, String.class);
            String tableName = action.get(AddColumnsAction.Attr.tableName, String.class);
            String[] columnNames = def.get(UniqueConstraintDefinition.Attr.columnNames, String[].class);
            String constraintName = def.get(UniqueConstraintDefinition.Attr.constraintName, String.class);


            returnActions.add(new AddUniqueConstraintAction(catalogName, schemaName, tableName, constraintName, columnNames));
        }
    }

    protected void addForeignKeyStatements(Action action, Scope scope, List<Action> returnActions) {
        ForeignKeyDefinition[] constraints = action.get(AddColumnsAction.Attr.foreignKeyDefinitions, new ForeignKeyDefinition[0]);

        for (ForeignKeyDefinition fkConstraint : constraints) {
            String refSchemaName = null;
            String refTableName;
            String refColName;
            String baseCatalogName = action.get(AddColumnsAction.Attr.catalogName, String.class);
            String baseSchemaName = action.get(AddColumnsAction.Attr.schemaName, String.class);
            String baseTableName = action.get(AddColumnsAction.Attr.tableName, String.class);
            String[] baseColumnNames = fkConstraint.get(ForeignKeyDefinition.Attr.columnNames, String[].class);


            String foreignKeyName = fkConstraint.get(ForeignKeyDefinition.Attr.foreignKeyName, String.class);
            String references = fkConstraint.get(ForeignKeyDefinition.Attr.references, String.class);

            if (references != null) {
                Matcher referencesMatcher = Pattern.compile("([\\w\\._]+)\\(([\\w_]+)\\)").matcher(references);
                if (!referencesMatcher.matches()) {
                    throw new UnexpectedLiquibaseException("Don't know how to find table and column names from " +references);
                }
                refTableName = referencesMatcher.group(1);
                refColName = referencesMatcher.group(2);
            } else {
                refTableName = fkConstraint.get(ForeignKeyDefinition.Attr.referencedTableName, String.class);
                refColName =  fkConstraint.get(ForeignKeyDefinition.Attr.referencedColumnNames, String.class);
            }

            if (refTableName.indexOf(".") > 0) {
                refSchemaName = refTableName.split("\\.")[0];
                refTableName = refTableName.split("\\.")[1];
            }

            List<String> refColumns = StringUtils.splitAndTrim(refColName, ",");
            returnActions.add(new AddForeignKeyConstraintAction(foreignKeyName, baseCatalogName, baseSchemaName, baseTableName, baseColumnNames, null, refSchemaName, refTableName, refColumns.toArray(new String[refColumns.size()])));
        }
    }

    protected String getDefaultValueClause(ColumnDefinition column, Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        Object defaultValue = column.get(ColumnDefinition.Attr.defaultValue, Object.class);
        if (defaultValue != null) {
            return "DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
        }
        return null;
    }
}
