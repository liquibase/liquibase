package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.*;
import liquibase.actionlogic.*;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.*;
import liquibase.util.CollectionUtil;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CreateTableLogic extends AbstractSqlBuilderLogic<CreateTableAction> {

    public static enum Clauses {
        tableName, columnsClause, primaryKey, tablespace, foreignKeyClauses, uniqueConstraintClauses, mainClauses,
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
    protected Class<CreateTableAction> getSupportedAction() {
        return CreateTableAction.class;
    }

    @Override
    public ValidationErrors validate(CreateTableAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("name", action.table)
                .checkForRequiredField("columns", action);
    }

    @Override
    public ActionResult execute(CreateTableAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new ExecuteSqlAction(generateSql(action, scope).toString()));
    }

    @Override
    protected StringClauses generateSql(CreateTableAction action, Scope scope) {
        Database database = scope.getDatabase();

        List<Action> additionalActions = new ArrayList<>();

        StringClauses createTable = new StringClauses(" ");
        createTable.append("CREATE TABLE");
        createTable.append(Clauses.tableName, database.escapeObjectName(action.table.name, Table.class));

        List<Column> columns = CollectionUtil.createIfNull(action.columns);
//        for (Column column : columns) {
//            if (ObjectUtil.defaultIfEmpty(column.isPrimaryKey, false)) {
//                primaryKeyColumnNames.add(column.columnName.name);
//            }
//        }

        StringClauses tableDefinition = new StringClauses("(", ", ", ")");
        int i = 0;
        for (Column column : columns) {
            StringClauses columnClause = generateColumnSql(column, action, scope, additionalActions);

            tableDefinition.append("column " + i++, columnClause);
        }

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

        if (action.primaryKey != null) {
            StringClauses primaryKey = new StringClauses(" ");
            if (database.supportsPrimaryKeyNames()) {
                String pkName = action.primaryKey.getName();
                if (pkName == null) {
                    pkName = database.generatePrimaryKeyName(action.table.getName());
                }
                if (pkName != null) {
                    primaryKey.append("CONSTRAINT");
                    primaryKey.append(database.escapeObjectName(pkName, Index.class));
                }
            }
            primaryKey.append("PRIMARY KEY");
            StringClauses columnClauses = new StringClauses("(", ", ", ")");
            for (PrimaryKey.PrimaryKeyColumn col : action.primaryKey.columns) {
                columnClauses.append(database.escapeObjectName(col.name, Column.class));
            }
            primaryKey.append("columns", columnClauses);

            tableDefinition.append(Clauses.primaryKey, primaryKey);
        }
//        }

        StringClauses foreignKeyClauses = new StringClauses(", ");
        int fkNum = 1;
        for (ForeignKey fk : CollectionUtil.createIfNull(action.foreignKeys)) {
            foreignKeyClauses.append("foreignKey" + (fkNum++), generateForeignKeySql(fk, action, scope));
        }
        tableDefinition.append(Clauses.foreignKeyClauses, foreignKeyClauses);

        StringClauses uniqueConstraintClauses = new StringClauses(", ");
//        for (UniqueConstraint uniqueConstraint : CollectionUtil.createIfNull(action.uniqueConstraints)) {
//            uniqueConstraintClauses.append("uniqueConstraint "+uniqueConstraint.columnNames, generateUniqueConstraintSql(uniqueConstraint, action, scope));
//        }
        tableDefinition.append(Clauses.uniqueConstraintClauses, uniqueConstraintClauses);
//    }


//        String sql = buffer.toString().replaceFirst(",\\s*$", "")+")";
//
//        if (database instanceof MySQLDatabase && mysqlTableOptionStartWith != null){
//        	LoggerFactory.getLogger(getClass()).info("[MySQL] Using last startWith statement ("+mysqlTableOptionStartWith.toString()+") as table option.");
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

        createTable.append(Clauses.columnsClause, tableDefinition);

        String tablespace = action.table.tablespace;
        if (tablespace != null && database.supportsTablespaces()) {
            createTable.append(Clauses.tablespace, "TABLESPACE " + tablespace);
        }

        return createTable;
    }

    protected StringClauses generateUniqueConstraintSql(UniqueConstraint uniqueConstraint, CreateTableAction action, Scope scope) {
        Database database = scope.getDatabase();
        StringClauses clauses = new StringClauses();
        String constraintName = uniqueConstraint.getName();

        if (constraintName != null) {
            clauses.append(UniqueConstraintClauses.constraintName, "CONSTRAINT " + database.escapeObjectName(constraintName, Index.class));
        }

        clauses.append("UNIQUE");

        clauses.append(UniqueConstraintClauses.columns, new StringClauses("(", ", ", ")").append(uniqueConstraint.columns, Column.class, scope));

        return clauses;
    }

    protected StringClauses generateForeignKeySql(ForeignKey foreignKey, CreateTableAction action, Scope scope) {
        StringClauses clauses = new StringClauses();
        final Database database = scope.getDatabase();

        String referencesString = StringUtils.join(foreignKey.columnChecks, ", ", new StringUtils.StringUtilsFormatter<ForeignKey.ForeignKeyColumnCheck>() {
            @Override
            public String toString(ForeignKey.ForeignKeyColumnCheck obj) {
                return database.escapeObjectName(obj.baseColumn.name, Column.class);
            }
        });

        clauses.append("FOREIGN KEY");
        clauses.append(ForeignKeyClauses.columns, "(" + StringUtils.join(foreignKey.columnChecks, ", ", new StringUtils.StringUtilsFormatter<ForeignKey.ForeignKeyColumnCheck>() {
            @Override
            public String toString(ForeignKey.ForeignKeyColumnCheck obj) {
                return database.escapeObjectName(obj.referencedColumn.name, Column.class);
            }
        }) + ")");
        clauses.append("REFERENCES");

        if (!referencesString.contains(".") && database.getDefaultSchemaName() != null && database.getOutputDefaultSchema()) {
            referencesString = database.getDefaultSchemaName() + "." + referencesString;
        }
        clauses.append(ForeignKeyClauses.referencesTarget, referencesString);

        if (foreignKey.deleteRule == ForeignKeyConstraintType.importedKeyCascade) {
            clauses.append("ON DELETE CASCADE");
        }

        if (ObjectUtil.defaultIfEmpty(foreignKey.initiallyDeferred, false)) {
            clauses.append("INITIALLY DEFERRED");
        }
        if (ObjectUtil.defaultIfEmpty(foreignKey.deferrable, false)) {
            clauses.append("DEFERRABLE");
        }

        return clauses;
    }

    protected StringClauses generateColumnSql(Column column, CreateTableAction action, Scope scope, List<Action> additionalActions) {
        Database database = scope.getDatabase();

        String columnName = column.getName();
        StringClauses columnClause = new StringClauses().append(database.escapeObjectName(columnName, Column.class));
        columnClause.append(column.type.toString());

        Column.AutoIncrementInformation autoIncrementInformation = column.autoIncrementInformation;


//        boolean isPrimaryKeyColumn = ObjectUtil.defaultIfEmpty(column.isPrimaryKey, false);
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

        Object defaultValue = column.defaultValue;

        // auto-increment columns, there should be no default value
        if (defaultValue != null && autoIncrementInformation == null) {
            String defaultValueString;
            if (defaultValue instanceof SequenceNextValueFunction) {
                defaultValueString = database.generateDatabaseFunctionValue((SequenceNextValueFunction) defaultValue);
            } else {
                defaultValueString = defaultValue.toString();
            }

            columnClause.append(ColumnClauses.defaultValue, "DEFAULT " + defaultValueString);
        }

        if (autoIncrementInformation != null) {
            // TODO: check if database supports auto increment on non primary key column
            if (database.supportsAutoIncrement()) {
                BigInteger startWith = autoIncrementInformation.startWith;
                BigInteger incrementBy = autoIncrementInformation.incrementBy;
                ActionLogic addAutoIncrementLogic = scope.getSingleton(ActionLogicFactory.class).getActionLogic(new AddAutoIncrementAction(), scope);

                StringClauses autoIncrementClause = ((AddAutoIncrementLogic) addAutoIncrementLogic).generateAutoIncrementClause(new Column.AutoIncrementInformation(startWith, incrementBy));

                columnClause.append(ColumnClauses.autoIncrement, autoIncrementClause);
//                if (!"".equals(autoIncrementClause)) {
//                    columnClause.append(ColumnClauses.autoIncrement, autoIncrementClause);
//                }

//                if( autoIncrementConstraint.getStartWith() != null ){
//                    if (database instanceof MySQLDatabase){
//                        mysqlTableOptionStartWith = autoIncrementConstraint.getStartWith();
//                    }
//                }
            } else {
                LoggerFactory.getLogger(getClass()).warn(database.getShortName() + " does not support autoincrement columns as requested for " + action.table.name);
            }
        }

        boolean nullable = ObjectUtil.defaultIfEmpty(column.nullable, true); //ObjectUtil.defaultIfEmpty(column.isPrimaryKey, false) || ObjectUtil.defaultIfEmpty(column.nullable, false);

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
