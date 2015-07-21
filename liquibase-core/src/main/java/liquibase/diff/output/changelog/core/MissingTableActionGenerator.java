package liquibase.diff.output.changelog.core;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateTableAction;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.ActionExecutor;
import liquibase.actionlogic.QueryResult;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.MissingObjectActionGenerator;
import liquibase.exception.ActionPerformException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.Snapshot;
import liquibase.snapshot.SnapshotFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.util.LiquibaseUtil;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class MissingTableActionGenerator implements MissingObjectActionGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Snapshot referenceSnapshot, Snapshot targetSnapshot, Scope scope) {
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
    public List<? extends Action> fixMissing(DatabaseObject missingObject, DiffOutputControl control, Snapshot referenceSnapshot, Snapshot targetSnapshot, Scope scope) {
        try {
            Table missingTable = (Table) missingObject;

            Scope referenceOfflineDatabaseScope = scope.child(Scope.Attr.database, DatabaseFactory.getInstance().fromSnapshot(referenceSnapshot));

            PrimaryKey primaryKey  = LiquibaseUtil.snapshotObject(PrimaryKey.class, missingTable.getObjectReference(), referenceOfflineDatabaseScope);

//        if (control.diffResult.getReferenceSnapshot().getDatabase().isLiquibaseTable(missingTable.getSchema().toCatalogAndSchema(), missingTable.getName())) {
//            continue;
//        }

            CreateTableAction action = createCreateTableChange();
            action.tableName = missingTable.getName();
            action.remarks = missingTable.remarks;
            action.primaryKey = primaryKey;

            for (Column column : LiquibaseUtil.snapshotAll(Column.class, missingTable.getObjectReference(), referenceOfflineDatabaseScope)) {
                action.columns.add(column);
    //            columnDefinition.columnName = column.getName();
    //
    //            LiquibaseDataType ldt = DataTypeFactory.getInstance().from(column.type, targetScope.getDatabase());
    //            DatabaseDataType ddt = ldt.toDatabaseDataType(referenceScope.getDatabase());
    //            columnDefinition.columnType = ddt.toString();
    //
    //            if (column.isAutoIncrement()) {
    //                columnDefinition.autoIncrementInformation = new AutoIncrementDefinition();
    //            }
    //
    //            // In MySQL, the primary key must be specified at creation for an autoincrement column
    //            if (column.isAutoIncrement() && primaryKey != null && primaryKey.getColumnNamesAsList().contains(column.getName())) {
    //                columnDefinition.isPrimaryKey = true;
    //                action.primaryKeyTablespace = primaryKey.getTablespace();
    ////todo:         action refactoring MySQL sets some primary key names as PRIMARY which is invalid
    ////                if (comparisonDatabase instanceof MySQLDatabase && "PRIMARY".equals(primaryKey.getName())) {
    ////                    constraintsConfig.setPrimaryKeyName(null);
    ////                } else  {
    //                    action.primaryKeyName = primaryKey.getSimpleName();
    ////                }
    //                control.setAlreadyHandledMissing(primaryKey);
    //                control.setAlreadyHandledMissing(primaryKey.getBackingIndex());
    //            } else if (column.nullable != null && !column.nullable) {
    //                columnDefinition.nullable = false;
    //            }
    //
    //            setDefaultValue(columnDefinition, column, referenceScope, targetScope);
    //
    //            if (column.remarks != null) {
    //                columnDefinition.remarks = column.remarks;
    //            }
    //
    //            action.addColumn(column);
    //            control.setAlreadyHandledMissing(column);
            }

            return Arrays.asList((Action) action);
        } catch (ActionPerformException e) {
            throw new UnexpectedLiquibaseException(e);
        }


    }

    public static void setDefaultValue(Column column, Scope referenceScope, Scope targetScope) {
//        LiquibaseDataType dataType = DataTypeFactory.getInstance().from(column.type, targetScope.getDatabase());

        Object defaultValue = column.defaultValue;
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
