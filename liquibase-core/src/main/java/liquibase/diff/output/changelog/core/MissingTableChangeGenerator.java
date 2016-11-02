package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.DateTimeType;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;

import java.math.BigInteger;
import java.util.Date;

public class MissingTableChangeGenerator extends AbstractChangeGenerator implements MissingObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Table.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
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
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Table missingTable = (Table) missingObject;

        PrimaryKey primaryKey = missingTable.getPrimaryKey();

//        if (control.diffResult.getReferenceSnapshot().getDatabase().isLiquibaseTable(missingTable.getSchema().toCatalogAndSchema(), missingTable.getName())) {
//            continue;
//        }

        CreateTableChange change = createCreateTableChange();
        change.setTableName(missingTable.getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(missingTable.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(missingTable.getSchema().getName());
        }
        if (missingTable.getRemarks() != null) {
            change.setRemarks(missingTable.getRemarks());
        }

        for (Column column : missingTable.getColumns()) {
            ColumnConfig columnConfig = new ColumnConfig();
            columnConfig.setName(column.getName());
            LiquibaseDataType ldt = DataTypeFactory.getInstance().from(column.getType(), referenceDatabase);
            DatabaseDataType ddt = ldt.toDatabaseDataType(comparisonDatabase);
            String typeString = ddt.toString();
            if (comparisonDatabase instanceof MSSQLDatabase) {
                typeString = comparisonDatabase.unescapeDataTypeString(typeString);
            }
            columnConfig.setType(typeString);

            if (column.isAutoIncrement()) {
                columnConfig.setAutoIncrement(true);
            }

            ConstraintsConfig constraintsConfig = null;
            // In MySQL, the primary key must be specified at creation for an autoincrement column
            if (column.isAutoIncrement() && primaryKey != null && primaryKey.getColumns().size() == 1 && primaryKey.getColumnNamesAsList().contains(column.getName())) {
                if (referenceDatabase instanceof MSSQLDatabase && primaryKey.getBackingIndex() != null && primaryKey.getBackingIndex().getClustered() != null && !primaryKey.getBackingIndex().getClustered()) {
                    // have to handle PK as a separate statement
                } else if (referenceDatabase instanceof PostgresDatabase && primaryKey.getBackingIndex() != null && primaryKey.getBackingIndex().getClustered() != null && primaryKey.getBackingIndex().getClustered()) {
                    // have to handle PK as a separate statement
                } else {
                    constraintsConfig = new ConstraintsConfig();
                    constraintsConfig.setPrimaryKey(true);
                    constraintsConfig.setPrimaryKeyTablespace(primaryKey.getTablespace());
                    // MySQL sets some primary key names as PRIMARY which is invalid
                    if (comparisonDatabase instanceof MySQLDatabase && "PRIMARY".equals(primaryKey.getName())) {
                        constraintsConfig.setPrimaryKeyName(null);
                    } else  {
                        constraintsConfig.setPrimaryKeyName(primaryKey.getName());
                    }
                    control.setAlreadyHandledMissing(primaryKey);
                    control.setAlreadyHandledMissing(primaryKey.getBackingIndex());
                }
            } else if (column.isNullable() != null && !column.isNullable()) {
                constraintsConfig = new ConstraintsConfig();
                constraintsConfig.setNullable(false);
            }

            if (constraintsConfig != null) {
                columnConfig.setConstraints(constraintsConfig);
            }

            setDefaultValue(columnConfig, column, referenceDatabase);

            if (column.getRemarks() != null) {
                columnConfig.setRemarks(column.getRemarks());
            }

            if (column.getAutoIncrementInformation() != null) {
                BigInteger startWith = column.getAutoIncrementInformation().getStartWith();
                BigInteger incrementBy = column.getAutoIncrementInformation().getIncrementBy();
                if (startWith != null && !startWith.equals(BigInteger.ONE)) {
                    columnConfig.setStartWith(startWith);
                }
                if (incrementBy != null && !incrementBy.equals(BigInteger.ONE)) {
                    columnConfig.setIncrementBy(incrementBy);
                }
            }

            change.addColumn(columnConfig);
            control.setAlreadyHandledMissing(column);
        }


        return new Change[] {
                change
        };
    }

    public static void setDefaultValue(ColumnConfig columnConfig, Column column, Database database) {
        LiquibaseDataType dataType = DataTypeFactory.getInstance().from(column.getType(), database);

        Object defaultValue = column.getDefaultValue();
        if (defaultValue == null) {
            // do nothing
        } else if (column.isAutoIncrement()) {
            // do nothing
        } else if (defaultValue instanceof Date) {
            columnConfig.setDefaultValueDate((Date) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            columnConfig.setDefaultValueBoolean(((Boolean) defaultValue));
        } else if (defaultValue instanceof Number) {
            columnConfig.setDefaultValueNumeric(((Number) defaultValue));
        } else if (defaultValue instanceof DatabaseFunction) {

            DatabaseFunction function = (DatabaseFunction) defaultValue;
            if ("current".equals(function.getValue())) {
              if (database instanceof InformixDatabase) {
                if (dataType instanceof DateTimeType) {
                  if (dataType.getAdditionalInformation() == null || dataType.getAdditionalInformation().length() == 0) {
                    if (dataType.getParameters() != null && dataType.getParameters().length > 0) {

                      String parameter = String.valueOf(dataType.getParameters()[0]);

                      if ("4365".equals(parameter)) {
                        function = new DatabaseFunction("current year to fraction(3)");
                      }

                      if ("3594".equals(parameter)) {
                        function = new DatabaseFunction("current year to second");
                      }

                      if ("3080".equals(parameter)) {
                        function = new DatabaseFunction("current year to minute");
                      }

                      if ("2052".equals(parameter)) {
                        function = new DatabaseFunction("current year to day");
                      }
                    }
                  }
                }
              }
            }

            columnConfig.setDefaultValueComputed(function);
        } else {
            String defaultValueString = null;
            try {
                defaultValueString = DataTypeFactory.getInstance().from(column.getType(), database).objectToSql(defaultValue, database);
            } catch (NullPointerException e) {
                throw e;
            }
            if (defaultValueString != null) {
                defaultValueString = defaultValueString.replaceFirst("'",
                        "").replaceAll("'$", "");
            }

            columnConfig.setDefaultValue(defaultValueString);
        }
    }

    protected CreateTableChange createCreateTableChange() {
        return new CreateTableChange();
    }
}
