package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

public class ChangedColumnChangeGenerator implements ChangedObjectChangeGenerator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Column.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                Table.class
        };
    }

    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {
                PrimaryKey.class
        };
    }

    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        if (((Column) changedObject).getRelation() instanceof View) {
            return null;
        }

        //TODO        for (Column column : diffResult.getObjectDiff(Column.class).getChanged()) {
//            if (!shouldModifyColumn(column)) {
//                continue;
//            }
//
//            boolean foundDifference = false;
//            Column referenceColumn = diffResult.getReferenceSnapshot().getColumn(column.getRelation().getSchema(), column.getRelation().getName(), column.getName());
//            if (column.isDataTypeDifferent(referenceColumn)) {
//                ModifyDataTypeChange change = new ModifyDataTypeChange();
//                change.setTableName(column.getRelation().getName());
//                if (diffOutputConfig.isIncludeCatalog()) {
//                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
//                }
//                if (diffOutputConfig.isIncludeSchema()) {
//                    change.setSchemaName(column.getRelation().getSchema().getName());
//                }
//                change.setColumnName(column.getName());
//                change.setNewDataType(referenceColumn.getType().toString());
//                changes.add(generateChangeSet(change));
//                foundDifference = true;
//            }
//            if (column.isNullabilityDifferent(referenceColumn)) {
//                if (referenceColumn.isNullable() == null
//                        || referenceColumn.isNullable()) {
//                    DropNotNullConstraintChange change = new DropNotNullConstraintChange();
//                    change.setTableName(column.getRelation().getName());
//                    if (diffOutputConfig.isIncludeCatalog()) {
//                        change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
//                    }
//                    if (diffOutputConfig.isIncludeSchema()) {
//                        change.setSchemaName(column.getRelation().getSchema().getName());
//                    }
//                    change.setColumnName(column.getName());
//                    change.setColumnDataType(referenceColumn.getType().toString());
//
//                    changes.add(generateChangeSet(change));
//                    foundDifference = true;
//                } else {
//                    AddNotNullConstraintChange change = new AddNotNullConstraintChange();
//                    change.setTableName(column.getRelation().getName());
//                    if (diffOutputConfig.isIncludeCatalog()) {
//                        change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
//                    }
//                    if (diffOutputConfig.isIncludeSchema()) {
//                        change.setSchemaName(column.getRelation().getSchema().getName());
//                    }
//                    change.setColumnName(column.getName());
//                    change.setColumnDataType(referenceColumn.getType().toString());
//
//                    Object defaultValue = column.getDefaultValue();
//                    String defaultValueString;
//                    if (defaultValue != null) {
//                        defaultValueString = DataTypeFactory.getInstance().from(column.getType()).objectToSql(defaultValue, diffResult.getComparisonSnapshot().getDatabase());
//
//                        if (defaultValueString != null) {
//                            change.setDefaultNullValue(defaultValueString);
//                        }
//                    }
//
//
//                    changes.add(generateChangeSet(change));
//                    foundDifference = true;
//                }
//
//            }
//            if (!foundDifference) {
//                throw new RuntimeException("Unknown difference");
//            }
//        }

        return null;
    }
}
