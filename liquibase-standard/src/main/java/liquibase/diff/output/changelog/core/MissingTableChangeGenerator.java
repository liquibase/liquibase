package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.DateTimeType;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;
import liquibase.structure.core.UniqueConstraint;

import java.math.BigInteger;
import java.util.*;

public class MissingTableChangeGenerator extends AbstractChangeGenerator implements MissingObjectChangeGenerator {

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
                        if ((dataType.getAdditionalInformation() == null) || dataType.getAdditionalInformation()
                                .isEmpty()) {
                            if ((dataType.getParameters() != null) && (dataType.getParameters().length > 0)) {

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

        columnConfig.setDefaultValueConstraintName(column.getDefaultValueConstraintName());
    }

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
        List<String> pkColumnList = ((primaryKey != null) ? primaryKey.getColumnNamesAsList() : null);
        Map<Column, UniqueConstraint> singleUniqueConstraints = getSingleColumnUniqueConstraints(missingTable);

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
        if (control.getIncludeTablespace() && (missingTable.getTablespace() != null) && comparisonDatabase.supportsTablespaces()) {
            change.setTablespace(missingTable.getTablespace());
        }

        if (referenceDatabase instanceof OracleDatabase && missingTable.getAttribute("temporary", "no").equals("GLOBAL")) {
                change.setTableType("GLOBAL TEMPORARY");
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

            boolean primaryKeyOrderMatchesTableOrder = checkPrimaryKeyOrderMatchesTableOrder(missingTable, pkColumnList);

            ConstraintsConfig constraintsConfig = null;
            // In MySQL, the primary key must be specified at creation for an autoincrement column, but it is not required to match the order of the table.
            if ((pkColumnList != null) && (primaryKeyOrderMatchesTableOrder || referenceDatabase instanceof MySQLDatabase) &&  pkColumnList.contains(column.getName())) {
                if ((referenceDatabase instanceof MSSQLDatabase) && (primaryKey.getBackingIndex() != null) &&
                        (primaryKey.getBackingIndex().getClustered() != null) && !primaryKey.getBackingIndex()
                        .getClustered()) {
                    // have to handle PK as a separate statement
                } else if ((referenceDatabase instanceof PostgresDatabase) && (primaryKey.getBackingIndex() != null)
                        && (primaryKey.getBackingIndex().getClustered() != null) && primaryKey.getBackingIndex()
                        .getClustered()) {
                    // have to handle PK as a separate statement
                } else {
                    constraintsConfig = new ConstraintsConfig();
                    if (shouldAddPrimarykeyToConstraints(missingObject, control, referenceDatabase, comparisonDatabase)) {
                        constraintsConfig.setPrimaryKey(true);
                        constraintsConfig.setPrimaryKeyTablespace(primaryKey.getTablespace());

                        // MySQL sets some primary key names as PRIMARY which is invalid
                        if ((comparisonDatabase instanceof MySQLDatabase) && "PRIMARY".equals(primaryKey.getName())) {
                            constraintsConfig.setPrimaryKeyName(null);
                        } else {
                            constraintsConfig.setPrimaryKeyName(primaryKey.getName());
                        }
                        control.setAlreadyHandledMissing(primaryKey);
                        control.setAlreadyHandledMissing(primaryKey.getBackingIndex());
                    } else {
                        constraintsConfig.setNullable(false);
                    }
                }
            }

            if ((column.isNullable() != null) && !column.isNullable()) {
                if (constraintsConfig == null) {
                    constraintsConfig = new ConstraintsConfig();
                }
                constraintsConfig.setNullable(false);
                if (!column.getValidateNullable()) {
                    constraintsConfig.setValidateNullable(false);
                }
                constraintsConfig.setNotNullConstraintName(column.getAttribute("notNullConstraintName", String.class));
            }

            if (referenceDatabase instanceof MySQLDatabase) {
                UniqueConstraint uniqueConstraint = singleUniqueConstraints.get(column);
                if (uniqueConstraint != null) {
                    if (!control.alreadyHandledMissing(uniqueConstraint, referenceDatabase)) {
                        if (constraintsConfig == null) {
                            constraintsConfig = new ConstraintsConfig();
                        }
                        constraintsConfig.setUnique(true);
                        control.setAlreadyHandledMissing(uniqueConstraint);
                        control.setAlreadyHandledMissing(uniqueConstraint.getBackingIndex());
                    }
                }
            }

            if (constraintsConfig != null) {
                columnConfig.setConstraints(constraintsConfig);
            }

            setDefaultValue(columnConfig, column, referenceDatabase);

            if (column.getRemarks() != null) {
                columnConfig.setRemarks(column.getRemarks());
            }

            Column.AutoIncrementInformation autoIncrementInfo = column.getAutoIncrementInformation();
            if (autoIncrementInfo != null) {
                BigInteger startWith = autoIncrementInfo.getStartWith();
                BigInteger incrementBy = autoIncrementInfo.getIncrementBy();
                String generationType = autoIncrementInfo.getGenerationType();
                Boolean defaultOnNull = autoIncrementInfo.getDefaultOnNull();
                if (!startWith.equals(BigInteger.ONE)) {
                    columnConfig.setStartWith(startWith);
                }
                if (!incrementBy.equals(BigInteger.ONE)) {
                    columnConfig.setIncrementBy(incrementBy);
                }
                if (StringUtil.isNotEmpty(generationType)) {
                    columnConfig.setGenerationType(generationType);
                    if (defaultOnNull != null) {
                        columnConfig.setDefaultOnNull(defaultOnNull);
                    }
                }
            }

            //
            // If there is a computed setting then use it
            //
            if (column.getComputed() != null) {
                columnConfig.setComputed(column.getComputed());
            }
            change.addColumn(columnConfig);
            control.setAlreadyHandledMissing(column);
        }

        // In SQLite, we must specify the PRIMARY KEY at table creation time


        return new Change[]{
                change
        };
    }

    private boolean checkPrimaryKeyOrderMatchesTableOrder(Table missingTable, List<String> pkColumnList) {
        if (pkColumnList == null) {
            return false;
        }

        int lastTableOrder = -1;
        final List<Column> tableColumnList = missingTable.getColumns();

        for (String pkColumnName : pkColumnList) {
            for (int i = 0; i < tableColumnList.size(); i++) {
                final Column tableColumn = tableColumnList.get(i);
                if (Objects.equals(tableColumn.getName(), pkColumnName)) {
                    if (i < lastTableOrder) {
                        return false;
                    }
                    lastTableOrder = i;
                }
            }
        }

        return true;
    }

    private Map<Column, UniqueConstraint> getSingleColumnUniqueConstraints(Table missingTable) {
        Map<Column, UniqueConstraint> map = new HashMap<>();
        List<UniqueConstraint> constraints = missingTable.getUniqueConstraints() == null ? null : missingTable.getUniqueConstraints();
        for (UniqueConstraint constraint : constraints) {
            if (constraint.getColumns().size() == 1) {
                map.put(constraint.getColumns().get(0), constraint);
            }
        }
        return map;
    }

    protected CreateTableChange createCreateTableChange() {
        return new CreateTableChange();
    }

    public boolean shouldAddPrimarykeyToConstraints(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase) {
        return true;
    }
}
