package liquibase.diff.output.changelog.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AutoIncrementDefinition;
import liquibase.action.core.ColumnDefinition;
import liquibase.action.core.CreateTableAction;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.MissingObjectActionGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MissingTableActionGenerator implements MissingObjectActionGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Scope referenceScope, Scope targetScope) {
        if (Table.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public List<? extends Action> fixMissing(DatabaseObject missingObject, DiffOutputControl control, Scope referenceScope, Scope targetScope) {
        Table missingTable = (Table) missingObject;

        PrimaryKey primaryKey = missingTable.getPrimaryKey();

//        if (control.diffResult.getReferenceSnapshot().getDatabase().isLiquibaseTable(missingTable.getSchema().toCatalogAndSchema(), missingTable.getName())) {
//            continue;
//        }

        CreateTableAction action = createCreateTableChange();
        action.set(CreateTableAction.Attr.tableName, missingTable.getName());
        action.set(CreateTableAction.Attr.remarks, missingTable.getRemarks());

        for (Column column : missingTable.getColumns()) {
            ColumnDefinition columnDefinition = new ColumnDefinition();
            columnDefinition.set(ColumnDefinition.Attr.columnName, column.getSimpleName());

            LiquibaseDataType ldt = DataTypeFactory.getInstance().from(column.getType(), targetScope.getDatabase());
            DatabaseDataType ddt = ldt.toDatabaseDataType(referenceScope.getDatabase());
            columnDefinition.set(ColumnDefinition.Attr.columnType, ddt.toString());

            if (column.isAutoIncrement()) {
                columnDefinition.set(ColumnDefinition.Attr.autoIncrementDefinition, new AutoIncrementDefinition());
            }

            // In MySQL, the primary key must be specified at creation for an autoincrement column
            if (column.isAutoIncrement() && primaryKey != null && primaryKey.getColumnNamesAsList().contains(column.getName())) {
                columnDefinition.set(ColumnDefinition.Attr.isPrimaryKey, true);
                action.set(CreateTableAction.Attr.primaryKeyTablespace, primaryKey.getTablespace());
//todo:         action refactoring MySQL sets some primary key names as PRIMARY which is invalid
//                if (comparisonDatabase instanceof MySQLDatabase && "PRIMARY".equals(primaryKey.getName())) {
//                    constraintsConfig.setPrimaryKeyName(null);
//                } else  {
                    action.set(CreateTableAction.Attr.primaryKeyName, primaryKey.getSimpleName());
//                }
                control.setAlreadyHandledMissing(primaryKey);
                control.setAlreadyHandledMissing(primaryKey.getBackingIndex());
            } else if (column.isNullable() != null && !column.isNullable()) {
                columnDefinition.set(ColumnDefinition.Attr.isNullable, false);
            }

            setDefaultValue(columnDefinition, column, referenceScope, targetScope);

            if (column.getRemarks() != null) {
                columnDefinition.set(ColumnDefinition.Attr.remarks, column.getRemarks());
            }

            action.addColumn(columnDefinition);
            control.setAlreadyHandledMissing(column);
        }


        return Arrays.asList((Action) action);
    }

    public static void setDefaultValue(ColumnDefinition columnDefinition, Column column, Scope referenceScope, Scope targetScope) {
        LiquibaseDataType dataType = DataTypeFactory.getInstance().from(column.getType(), targetScope.getDatabase());

        Object defaultValue = column.getDefaultValue();
//todo: action refactoring        if (defaultValue == null) {
//            // do nothing
//        } else if (column.isAutoIncrement()) {
//            // do nothing
//        } else if (defaultValue instanceof Date) {
//            columnDefinition.setDefaultValueDate((Date) defaultValue);
//        } else if (defaultValue instanceof Boolean) {
//            columnDefinition.setDefaultValueBoolean(((Boolean) defaultValue));
//        } else if (defaultValue instanceof Number) {
//            columnDefinition.setDefaultValueNumeric(((Number) defaultValue));
//        } else if (defaultValue instanceof DatabaseFunction) {

//            DatabaseFunction function = (DatabaseFunction) defaultValue;
//            if ("current".equals(function.getValue())) {
//              if (database instanceof InformixDatabase) {
//                if (dataType instanceof DateTimeType) {
//                  if (dataType.getAdditionalInformation() == null || dataType.getAdditionalInformation().length() == 0) {
//                    if (dataType.getParameters() != null && dataType.getParameters().length > 0) {
//
//                      String parameter = String.valueOf(dataType.getParameters()[0]);
//
//                      if ("4365".equals(parameter)) {
//                        function = new DatabaseFunction("current year to fraction(3)");
//                      }
//
//                      if ("3594".equals(parameter)) {
//                        function = new DatabaseFunction("current year to second");
//                      }
//
//                      if ("3080".equals(parameter)) {
//                        function = new DatabaseFunction("current year to minute");
//                      }
//
//                      if ("2052".equals(parameter)) {
//                        function = new DatabaseFunction("current year to day");
//                      }
//                    }
//                  }
//                }
//              }
//            }

//            columnDefinition.setDefaultValueComputed(function);
//        } else {
//            columnDefinition.set(ColumnDefinition.Attr.defaultValue, defaultValue.toString());
//        }
    }

    protected CreateTableAction createCreateTableChange() {
        return new CreateTableAction();
    }
}
