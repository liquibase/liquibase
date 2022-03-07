package liquibase.diff.output.changelog.core;

import liquibase.Scope;
import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.core.*;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.BooleanType;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.ISODateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ChangedColumnChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Column.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                Table.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {
                PrimaryKey.class
        };
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Column column = (Column) changedObject;
        if (column.getRelation() instanceof View) {
            return null;
        }

        if (column.getRelation().getSnapshotId() == null) { //not an actual table, maybe an alias, maybe in a different schema. Don't fix it.
            return null;
        }

        List<Change> changes = new ArrayList<>();

        handleTypeDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);
        handleNullableDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);
        handleDefaultValueDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);
        handleAutoIncrementDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);

        Difference remarksDiff = differences.getDifference("remarks");
        if (remarksDiff != null) {
            SetColumnRemarksChange change = new SetColumnRemarksChange();
            if (control.getIncludeCatalog()) {
                change.setCatalogName(column.getSchema().getCatalogName());
            }
            if (control.getIncludeSchema()) {
                change.setSchemaName(column.getSchema().getName());
            }
            change.setTableName(column.getRelation().getName());
            change.setColumnName(column.getName());
            change.setRemarks(column.getRemarks());

            LiquibaseDataType columnDataType = DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase);
            if (columnDataType != null) {
            change.setColumnDataType(columnDataType.toString());
            }

            changes.add(change);
        }

        return changes.toArray(new Change[changes.size()]);
    }

    protected void handleNullableDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<Change> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference nullableDifference = differences.getDifference("nullable");
        if ((nullableDifference != null) && (nullableDifference.getReferenceValue() != null)) {
            boolean nullable = (Boolean) nullableDifference.getReferenceValue();
            if (nullable) {
                DropNotNullConstraintChange change = new DropNotNullConstraintChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase).toString());
                changes.add(change);
            } else {
                AddNotNullConstraintChange change = new AddNotNullConstraintChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase).toString());
                change.setValidate(column.getValidate());
                change.setConstraintName(column.getAttribute("notNullConstraintName", String.class));
                changes.add(change);
            }
        }
    }

    protected void handleAutoIncrementDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<Change> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference difference = differences.getDifference("autoIncrementInformation");
        if (difference != null) {
            if (difference.getReferenceValue() == null) {
                Scope.getCurrentScope().getLog(getClass()).info("ChangedColumnChangeGenerator cannot fix dropped auto increment values");
                //todo: Support dropping auto increments
            } else {
                AddAutoIncrementChange change = new AddAutoIncrementChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase).toString());
                changes.add(change);
            }
        }
    }

    protected void handleTypeDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<Change> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference typeDifference = differences.getDifference("type");
        if (typeDifference != null) {
            String catalogName = null;
            String schemaName = null;
            if (control.getIncludeCatalog()) {
                catalogName = column.getRelation().getSchema().getCatalog().getName();
            }
            if (control.getIncludeSchema()) {
                schemaName = column.getRelation().getSchema().getName();
            }


            String tableName = column.getRelation().getName();

            if ((comparisonDatabase instanceof OracleDatabase) && ("clob".equalsIgnoreCase(((DataType) typeDifference
                .getReferenceValue()).getTypeName()) || "clob".equalsIgnoreCase(((DataType) typeDifference
                .getComparedValue()).getTypeName()))) {
                String tempColName = "TEMP_CLOB_CONVERT";
                OutputChange outputChange = new OutputChange();
                outputChange.setMessage("Cannot convert directly from " + ((DataType) typeDifference.getComparedValue()).getTypeName()+" to "+((DataType) typeDifference.getReferenceValue()).getTypeName()+". Instead a new column will be created and the data transferred. This may cause unexpected side effects including constraint issues and/or table locks.");
                changes.add(outputChange);

                AddColumnChange addColumn = new AddColumnChange();
                addColumn.setCatalogName(catalogName);
                addColumn.setSchemaName(schemaName);
                addColumn.setTableName(tableName);
                AddColumnConfig addColumnConfig = new AddColumnConfig(column);
                addColumnConfig.setName(tempColName);
                addColumnConfig.setType(typeDifference.getReferenceValue().toString());
                addColumnConfig.setAfterColumn(column.getName());
                addColumn.setColumns(Arrays.asList(addColumnConfig));
                changes.add(addColumn);

                changes.add(new RawSQLChange("UPDATE "+referenceDatabase.escapeObjectName(tableName, Table.class)+" SET "+tempColName+"="+referenceDatabase.escapeObjectName(column.getName(), Column.class)));

                DropColumnChange dropColumnChange = new DropColumnChange();
                dropColumnChange.setCatalogName(catalogName);
                dropColumnChange.setSchemaName(schemaName);
                dropColumnChange.setTableName(tableName);
                dropColumnChange.setColumnName(column.getName());
                changes.add(dropColumnChange);

                RenameColumnChange renameColumnChange = new RenameColumnChange();
                renameColumnChange.setCatalogName(catalogName);
                renameColumnChange.setSchemaName(schemaName);
                renameColumnChange.setTableName(tableName);
                renameColumnChange.setOldColumnName(tempColName);
                renameColumnChange.setNewColumnName(column.getName());
                changes.add(renameColumnChange);

            } else {
                if ((comparisonDatabase instanceof MSSQLDatabase) && (column.getDefaultValue() != null)) { //have to drop the default value, will be added back with the "data type changed" logic.
                    DropDefaultValueChange dropDefaultValueChange = new DropDefaultValueChange();
                    dropDefaultValueChange.setCatalogName(catalogName);
                    dropDefaultValueChange.setSchemaName(schemaName);
                    dropDefaultValueChange.setTableName(tableName);
                    dropDefaultValueChange.setColumnName(column.getName());
                    changes.add(dropDefaultValueChange);
                }

                if (!isPostgresAutoIncrementEquivalentType(comparisonDatabase, typeDifference, column)) {
                    ModifyDataTypeChange change = new ModifyDataTypeChange();
                    change.setCatalogName(catalogName);
                    change.setSchemaName(schemaName);
                    change.setTableName(tableName);
                    change.setColumnName(column.getName());
                    DataType referenceType = (DataType) typeDifference.getReferenceValue();
                    change.setNewDataType(DataTypeFactory.getInstance().from(referenceType, comparisonDatabase).toString());

                    changes.add(change);
                }
            }
        }
    }

    /**
     * Determine if the type difference is essentially an equivalent type for auto-incrementing columns.
     * @param comparisonDatabase the database that is being compared
     * @param typeDifference the difference between the types
     * @param column the column for which the type difference occurs
     * @return true if the types are essentially equivalent (bigserial and int8/bigint would be considered equivalent),
     * false otherwise
     */
    private boolean isPostgresAutoIncrementEquivalentType(Database comparisonDatabase, Difference typeDifference, Column column) {
        if (comparisonDatabase instanceof PostgresDatabase && column.isAutoIncrement()) {
            String referenceType = ((DataType) typeDifference.getReferenceValue()).getTypeName();
            String comparisonType = ((DataType) typeDifference.getComparedValue()).getTypeName();
            return isPostgresAutoIncrementEquivalentType(referenceType, comparisonType) || isPostgresAutoIncrementEquivalentType(comparisonType, referenceType);
        } else {
            return false;
        }
    }

    /**
     * Determine if the two types are essentially equivalent.
     * @param type1 first type to compare
     * @param type2 second type to compare
     * @return true if the types are essentially equivalent (bigserial and int8/bigint would be considered equivalent),
     * false otherwise
     */
    private boolean isPostgresAutoIncrementEquivalentType(String type1, String type2) {
        if (type1.equalsIgnoreCase(type2)) {
            return true;
        } else if (type1.equalsIgnoreCase("bigserial")) {
            return type2.equalsIgnoreCase("bigserial") || type2.equalsIgnoreCase("int8");
        } else if (type1.equalsIgnoreCase("serial")) {
            return type2.equalsIgnoreCase("serial") || type2.equalsIgnoreCase("int4");
        } else if (type1.equalsIgnoreCase("smallserial")) {
            return type2.equalsIgnoreCase("smallserial") || type2.equalsIgnoreCase("int2");
        }
        return false;
    }

    protected void handleDefaultValueDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<Change> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference difference = differences.getDifference("defaultValue");

        if (difference != null) {
            Object value = difference.getReferenceValue();

            LiquibaseDataType columnDataType = DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase);
            if (value == null) {
                DropDefaultValueChange change = new DropDefaultValueChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(columnDataType.toString());

                changes.add(change);

            } else if (shouldTriggerAddDefaultChange(column, difference, comparisonDatabase)) {
                AddDefaultValueChange change = new AddDefaultValueChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(columnDataType.toString());

                //
                // Make sure we handle BooleanType values which are not Boolean
                //
                if (value instanceof Boolean || columnDataType instanceof BooleanType) {
                    if (value instanceof Boolean) {
                        change.setDefaultValueBoolean((Boolean) value);
                    }
                    else if (columnDataType instanceof BooleanType) {
                        if (value instanceof DatabaseFunction) {
                            if (value.equals(new DatabaseFunction("'false'"))) {
                                change.setDefaultValueBoolean(false);
                            } else if (value.equals(new DatabaseFunction("'true'"))) {
                                change.setDefaultValueBoolean(true);
                            } else {
                                change.setDefaultValueComputed(((DatabaseFunction) value));
                            }
                        }
                    }
                } else if (value instanceof Date) {
                    change.setDefaultValueDate(new ISODateFormat().format(((Date) value)));
                } else if (value instanceof Number) {
                    change.setDefaultValueNumeric(value.toString());
                } else if (value instanceof DatabaseFunction) {
                    change.setDefaultValueComputed(((DatabaseFunction) value));
                } else {
                    change.setDefaultValue(value.toString());
                }
                change.setDefaultValueConstraintName(column.getDefaultValueConstraintName());


                changes.add(change);
            }
        }
    }

    /**
     * For {@link PostgresDatabase} if column is of autoIncrement/SERIAL type we can ignore 'defaultValue' differences
     * (because its execution of sequence.next() anyway)
     */
    private boolean shouldTriggerAddDefaultChange(Column column, Difference difference, Database comparisonDatabase) {
        if (!(comparisonDatabase instanceof PostgresDatabase)) {
            return true;
        }
        if (column.getAutoIncrementInformation() != null && difference.getReferenceValue() instanceof DatabaseFunction) {
            return false;
        }
        return true;
    }
}
