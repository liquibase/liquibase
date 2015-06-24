package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.*;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.ISODateFormat;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChangedColumnChangeGenerator implements ChangedObjectChangeGenerator {
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
//        if (column.relation instanceof View) {
//            return null;
//        }
//
//        if (column.relation.getSnapshotId() == null) { //not an actual table, maybe an alias, maybe in a different schema. Don't fix it.
//            return null;
//        }

        List<Change> changes = new ArrayList<Change>();

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

            changes.add(change);
        }

        return changes.toArray(new Change[changes.size()]);
    }

    protected void handleNullableDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<Change> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference nullableDifference = differences.getDifference("nullable");
        if (nullableDifference != null && nullableDifference.getReferenceValue() != null) {
            boolean nullable = (Boolean) nullableDifference.getReferenceValue();
            if (nullable) {
                DropNotNullConstraintChange change = new DropNotNullConstraintChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getCatalogName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getSchemaName());
                }
                change.setTableName(column.getRelationName());
                change.setColumnName(column.getSimpleName());
                change.setColumnDataType(DataTypeFactory.getInstance().from(column.type, comparisonDatabase).toString());
                changes.add(change);
            } else {
                AddNotNullConstraintChange change = new AddNotNullConstraintChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getCatalogName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getSchemaName());
                }
                change.setTableName(column.getRelationName());
                change.setColumnName(column.getSimpleName());
                change.setColumnDataType(DataTypeFactory.getInstance().from(column.type, comparisonDatabase).toString());
                changes.add(change);
            }
        }
    }

    protected void handleAutoIncrementDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<Change> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference difference = differences.getDifference("autoIncrementInformation");
        if (difference != null) {
            if (difference.getReferenceValue() == null) {
                LoggerFactory.getLogger(getClass()).info("ChangedColumnChangeGenerator cannot fix dropped auto increment values");
                //todo: Support dropping auto increments
            } else {
                AddAutoIncrementChange change = new AddAutoIncrementChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getCatalogName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getSchemaName());
                }
                change.setTableName(column.getRelationName());
                change.setColumnName(column.getSimpleName());
                change.setColumnDataType(DataTypeFactory.getInstance().from(column.type, comparisonDatabase).toString());
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
                catalogName = column.getCatalogName();
            }
            if (control.getIncludeSchema()) {
                schemaName = column.getSchemaName();
            }

            String tableName = column.getRelationName();

//            if (comparisonDatabase instanceof OracleDatabase && (((DataType) typeDifference.getReferenceValue()).getTypeName().equalsIgnoreCase("clob") || ((DataType) typeDifference.getComparedValue()).getTypeName().equalsIgnoreCase("clob"))) {
//                String tempColName = "TEMP_CLOB_CONVERT";
//                OutputChange outputChange = new OutputChange();
//                outputChange.setMessage("Cannot convert directly from " + ((DataType) typeDifference.getComparedValue()).getTypeName()+" to "+((DataType) typeDifference.getReferenceValue()).getTypeName()+". Instead a new column will be created and the data transferred. This may cause unexpected side effects including constraint issues and/or table locks.");
//                changes.add(outputChange);
//
//                AddColumnChange addColumn = new AddColumnChange();
//                addColumn.setCatalogName(catalogName);
//                addColumn.setSchemaName(schemaName);
//                addColumn.setTableName(tableName);
//                AddColumnConfig addColumnConfig = new AddColumnConfig(column);
//                addColumnConfig.setName(tempColName);
//                addColumnConfig.setType(typeDifference.getReferenceValue().toString());
//                addColumnConfig.setAfterColumn(column.getName());
//                addColumn.setColumns(Arrays.asList(addColumnConfig));
//                changes.add(addColumn);
//
//                changes.add(new RawSQLChange("UPDATE "+referenceDatabase.escapeObjectName(tableName, Table.class)+" SET "+tempColName+"="+referenceDatabase.escapeObjectName(column.getName(), Column.class)));
//
//                DropColumnChange dropColumnChange = new DropColumnChange();
//                dropColumnChange.setCatalogName(catalogName);
//                dropColumnChange.setSchemaName(schemaName);
//                dropColumnChange.setTableName(tableName);
//                dropColumnChange.setColumnName(column.getName());
//                changes.add(dropColumnChange);
//
//                RenameColumnChange renameColumnChange = new RenameColumnChange();
//                renameColumnChange.setCatalogName(catalogName);
//                renameColumnChange.setSchemaName(schemaName);
//                renameColumnChange.setTableName(tableName);
//                renameColumnChange.setOldColumnName(tempColName);
//                renameColumnChange.setNewColumnName(column.getName());
//                changes.add(renameColumnChange);
//
//            } else {
                ModifyDataTypeChange change = new ModifyDataTypeChange();
                change.setCatalogName(catalogName);
                change.setSchemaName(schemaName);
                change.setTableName(tableName);
                change.setColumnName(column.getSimpleName());
                DataType referenceType = (DataType) typeDifference.getReferenceValue();
                change.setNewDataType(DataTypeFactory.getInstance().from(referenceType, comparisonDatabase).toString());

                changes.add(change);
//            }
        }
    }

    protected void handleDefaultValueDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<Change> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference difference = differences.getDifference("defaultValue");

        if (difference != null) {
            Object value = difference.getReferenceValue();

            LiquibaseDataType columnDataType = DataTypeFactory.getInstance().from(column.type, comparisonDatabase);
            if (value == null) {
                DropDefaultValueChange change = new DropDefaultValueChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getCatalogName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getSchemaName());
                }
                change.setTableName(column.getRelationName());
                change.setColumnName(column.getSimpleName());
                change.setColumnDataType(columnDataType.toString());

                changes.add(change);

            } else {
                AddDefaultValueChange change = new AddDefaultValueChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getCatalogName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getSchemaName());
                }
                change.setTableName(column.getRelationName());
                change.setColumnName(column.getSimpleName());
                change.setColumnDataType(columnDataType.toString());

                if (value instanceof Boolean) {
                    change.setDefaultValueBoolean((Boolean) value);
                } else if (value instanceof Date) {
                    change.setDefaultValueDate(new ISODateFormat().format(((Date) value)));
                } else if (value instanceof Number) {
                    change.setDefaultValueNumeric(value.toString());
                } else if (value instanceof DatabaseFunction) {
                    change.setDefaultValueComputed(((DatabaseFunction) value));
                } else {
                    change.setDefaultValue(value.toString());
                }


                changes.add(change);
            }
        }
    }
}
