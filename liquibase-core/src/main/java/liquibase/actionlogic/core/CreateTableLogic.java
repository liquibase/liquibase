package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.*;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CreateTableLogic extends AbstractSqlBuilderLogic {

    public static enum Clauses {
        tableName, definitionStart, postColumnListSeparator, primaryKey, tablespace,
    }

    public static enum ColumnClauses {
        autoIncrement, nullable, defaultValue
    }

    public static enum UniqueConstraintClauses {
        constraintName, columns
    }

    public static enum ForeignKeyClauses {
        constraintName, referencesTarget, columns
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return CreateTableAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkForRequiredField(CreateTableAction.Attr.tableName, action);
        validationErrors.checkForRequiredField(CreateTableAction.Attr.columnDefinitions, action);

        return validationErrors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new ExecuteSqlAction(generateSql(action, scope).toString()));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        List<Action> additionalActions = new ArrayList<>();

        StringClauses clauses = new StringClauses();
        clauses.append("CREATE TABLE");
        clauses.append(Clauses.tableName, database.escapeTableName(action.get(CreateTableAction.Attr.catalogName, String.class), action.get(CreateTableAction.Attr.schemaName, String.class), action.get(CreateTableAction.Attr.tableName, String.class)));
        clauses.append(Clauses.definitionStart, "(");

        List<String> primaryKeyColumnNames = new ArrayList<>();
        List<ColumnDefinition> columns = action.get(CreateTableAction.Attr.columnDefinitions, new ArrayList<ColumnDefinition>());
        for (ColumnDefinition column : columns) {
            if (column.get(ColumnDefinition.Attr.isPrimaryKey, false)) {
                primaryKeyColumnNames.add(column.get(ColumnDefinition.Attr.columnName, String.class));
            }
        }

        int i=0;
        for (ColumnDefinition column : columns) {
            StringClauses columnClause = generateColumnSql(column, action, scope, additionalActions);

            clauses.append("column "+column.get(ColumnDefinition.Attr.columnName, String.class), columnClause.toString()+(i++ == columns.size()?"":", "));
        }

        clauses.append(Clauses.postColumnListSeparator, ", ");

//        if (!( (database instanceof SQLiteDatabase) &&
//                isSinglePrimaryKeyColumn &&
//                isPrimaryKeyAutoIncrement) &&
//
//                !((database instanceof InformixDatabase) &&
//                isSinglePrimaryKeyColumn
//                )) {
            // ...skip this code block for sqlite if a single column primary key
            // with an autoincrement constraint exists.
            // This constraint is added after the column type.

            if (primaryKeyColumnNames.size() > 1) {
                StringBuilder primaryKey = new StringBuilder();
                if (database.supportsPrimaryKeyNames()) {
                    String pkName = action.get(CreateTableAction.Attr.primaryKeyName, String.class);
                    if (pkName == null) {
                        pkName = database.generatePrimaryKeyName(action.get(CreateTableAction.Attr.tableName, String.class));
                    }
                    if (pkName != null) {
                        primaryKey.append("CONSTRAINT ");
                        primaryKey.append(database.escapeConstraintName(pkName));
                    }
                }
                primaryKey.append(" PRIMARY KEY (");
                primaryKey.append(database.escapeColumnNameList(StringUtils.join(primaryKeyColumnNames, ", ")));
                primaryKey.append(")");

                clauses.append(Clauses.primaryKey, primaryKey.toString()+",");
            }
//        }

        for (ForeignKeyDefinition fk : action.get(CreateTableAction.Attr.foreignKeyDefinitions, new ArrayList<ForeignKeyDefinition>())) {
            clauses.append("foreignKey "+fk.get(ForeignKeyDefinition.Attr.columnNames, String.class), generateForeignKeySql(fk, action, scope).toString()+", ");
        }

        for (UniqueConstraintDefinition uniqueConstraint : action.get(CreateTableAction.Attr.uniqueConstraintDefinitions, new ArrayList<UniqueConstraintDefinition>())) {
            clauses.append(generateUniqueConstraintSql(uniqueConstraint, action, scope).toString()+",");
        }
//    }


//        String sql = buffer.toString().replaceFirst(",\\s*$", "")+")";
//
//        if (database instanceof MySQLDatabase && mysqlTableOptionStartWith != null){
//        	LogFactory.getLogger().info("[MySQL] Using last startWith statement ("+mysqlTableOptionStartWith.toString()+") as table option.");
//        	sql += " "+((MySQLDatabase)database).getTableOptionAutoIncrementStartWithClause(mysqlTableOptionStartWith);
//        }


//        if (StringUtils.trimToNull(tablespace) != null && database.supportsTablespaces()) {
//            if (database instanceof MSSQLDatabase) {
//                buffer.append(" ON ").append(tablespace);
//            } else if (database instanceof DB2Database) {
//                buffer.append(" IN ").append(tablespace);
//            } else {
//                buffer.append(" TABLESPACE ").append(tablespace);
//            }
//        }

        String tablespace = action.get(CreateTableAction.Attr.tablespace, String.class);
        if (tablespace != null && database.supportsTablespaces()) {
            clauses.append(Clauses.tablespace, "TABLESPACE "+tablespace);
        }

        return clauses;
    }

    protected StringClauses generateUniqueConstraintSql(UniqueConstraintDefinition uniqueConstraint, Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        StringClauses clauses = new StringClauses();
        String constraintName = uniqueConstraint.get(UniqueConstraintDefinition.Attr.constraintName, String.class);

        if (constraintName != null) {
            clauses.append(UniqueConstraintClauses.constraintName, "CONSTRAINT "+database.escapeConstraintName(constraintName));
        }

        clauses.append("UNIQUE");

        clauses.append(UniqueConstraintClauses.columns, "("+database.escapeColumnNameList(StringUtils.join(uniqueConstraint.get(UniqueConstraintDefinition.Attr.columnNames, new ArrayList<String>()), ", "))+")");

        return clauses;
    }

    protected StringClauses generateForeignKeySql(ForeignKeyDefinition foreignKeyDefinition, Action action, Scope scope) {
        StringClauses clauses = new StringClauses();
        Database database = scope.get(Scope.Attr.database, Database.class);

        String name = foreignKeyDefinition.get(ForeignKeyDefinition.Attr.foreignKeyName, String.class);
        if (name != null) {
            clauses.append(ForeignKeyClauses.constraintName, "CONSTRAINT "+database.escapeConstraintName(name));
        }

        String referencesString = foreignKeyDefinition.get(ForeignKeyDefinition.Attr.references, String.class);

        clauses.append("FOREIGN KEY");
        clauses.append(ForeignKeyClauses.columns, "(" + StringUtils.join(foreignKeyDefinition.get(ForeignKeyDefinition.Attr.columnNames, new ArrayList<String>()), ", ", new StringUtils.ObjectNameFormatter(Column.class, database))+")");
        clauses.append("REFERENCES");

        if (referencesString != null) {
            if (!referencesString.contains(".") && database.getDefaultSchemaName() != null && database.getOutputDefaultSchema()) {
                referencesString = database.getDefaultSchemaName() +"."+referencesString;
            }
            clauses.append(ForeignKeyClauses.referencesTarget, referencesString);
        } else {
            clauses.append(database.escapeObjectName(foreignKeyDefinition.get(ForeignKeyDefinition.Attr.referencedTableName, String.class), Table.class)
                    + "(" +
                    database.escapeColumnNameList(foreignKeyDefinition.get(ForeignKeyDefinition.Attr.referencedColumnNames, String.class))
                    + ")");
        }


        if (foreignKeyDefinition.get(ForeignKeyDefinition.Attr.deleteCascade, false)) {
            clauses.append("ON DELETE CASCADE");
        }

        if (foreignKeyDefinition.get(ForeignKeyDefinition.Attr.initiallyDeferred, false)) {
            clauses.append("INITIALLY DEFERRED");
        }
        if (foreignKeyDefinition.get(ForeignKeyDefinition.Attr.deferrable, false)) {
            clauses.append("DEFERRABLE");
        }

        return clauses;
    }

    protected StringClauses generateColumnSql(ColumnDefinition column, Action action, Scope scope, List<Action> additionalActions) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        String columnName = column.get(ColumnDefinition.Attr.columnName, String.class);
        StringClauses columnClause = new StringClauses().append(database.escapeObjectName(columnName, Column.class));
        columnClause.append(column.get(ColumnDefinition.Attr.columnType, String.class));

        AutoIncrementDefinition autoIncrementDefinition = column.get(ColumnDefinition.Attr.autoIncrementDefinition, AutoIncrementDefinition.class);


        boolean isPrimaryKeyColumn = column.get(ColumnDefinition.Attr.isPrimaryKey, false);
//        boolean isPrimaryKeyAutoIncrement = isPrimaryKeyColumn && isAutoIncrementColumn;

//            if ((database instanceof SQLiteDatabase) &&
//                    isSinglePrimaryKeyColumn &&
//                    isPrimaryKeyColumn &&
//                    isAutoIncrementColumn) {
//                String pkName = StringUtils.trimToNull(statement.getPrimaryKeyConstraint().getConstraintName());
//                if (pkName == null) {
//                    pkName = database.generatePrimaryKeyName(statement.getTableName());
//                }
//                if (pkName != null) {
//                    buffer.append(" CONSTRAINT ");
//                    buffer.append(database.escapeConstraintName(pkName));
//                }
//                buffer.append(" PRIMARY KEY");
//            }

        Object defaultValue = column.get(ColumnDefinition.Attr.defaultValue, Object.class);

        // auto-increment columns, there should be no default value
        if (defaultValue != null && autoIncrementDefinition == null) {
            String defaultValueString;
            if (defaultValue instanceof SequenceNextValueFunction) {
                defaultValueString = database.generateDatabaseFunctionValue((SequenceNextValueFunction) defaultValue);
            } else {
                defaultValueString = defaultValue.toString();
            }

            columnClause.append(ColumnClauses.defaultValue, "DEFAULT "+defaultValueString);
        }

        if (autoIncrementDefinition != null) {
            // TODO: check if database supports auto increment on non primary key column
            if (database.supportsAutoIncrement()) {
                BigInteger startWith = autoIncrementDefinition.get(AutoIncrementDefinition.Attr.startWith, BigInteger.class);
                BigInteger incrementBy = autoIncrementDefinition.get(AutoIncrementDefinition.Attr.incrementBy, BigInteger.class);
                String autoIncrementClause = database.getAutoIncrementClause(startWith, incrementBy);

                if (!"".equals(autoIncrementClause)) {
                    columnClause.append(ColumnClauses.autoIncrement, autoIncrementClause);
                }

//                if( autoIncrementConstraint.getStartWith() != null ){
//                    if (database instanceof MySQLDatabase){
//                        mysqlTableOptionStartWith = autoIncrementConstraint.getStartWith();
//                    }
//                }
            } else {
                LogFactory.getLogger().warning(database.getShortName()+" does not support autoincrement columns as requested for "+(database.escapeTableName(action.get(CreateTableAction.Attr.catalogName, String.class), action.get(CreateTableAction.Attr.schemaName, String.class), action.get(CreateTableAction.Attr.tableName, String.class))));
            }
        }

        boolean nullable =  column.get(ColumnDefinition.Attr.isPrimaryKey, false) || column.get(ColumnDefinition.Attr.isNullable, false);

        if (nullable) {
            if (database.requiresDefiningColumnsAsNull()) {
                columnClause.append(ColumnClauses.nullable, "NULL");
            }
        } else {
            columnClause.append(ColumnClauses.nullable, "NOT NULL");
        }

        return columnClause;
    }


}
