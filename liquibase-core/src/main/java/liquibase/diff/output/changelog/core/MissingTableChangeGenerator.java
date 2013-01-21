package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.database.Database;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import java.util.Date;

public class MissingTableChangeGenerator implements MissingObjectChangeGenerator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Table.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Table missingTable = (Table) missingObject;

//        if (control.diffResult.getReferenceSnapshot().getDatabase().isLiquibaseTable(missingTable.getSchema().toCatalogAndSchema(), missingTable.getName())) {
//            continue;
//        }

        CreateTableChange change = new CreateTableChange();
        change.setTableName(missingTable.getName());
        if (control.isIncludeCatalog()) {
            change.setCatalogName(missingTable.getSchema().getCatalogName());
        }
        if (control.isIncludeSchema()) {
            change.setSchemaName(missingTable.getSchema().getName());
        }
        if (missingTable.getRemarks() != null) {
            change.setRemarks(missingTable.getRemarks());
        }

        for (Column column : missingTable.getColumns()) {
            ColumnConfig columnConfig = new ColumnConfig();
            columnConfig.setName(column.getName());
            columnConfig.setType(column.getType().toString());

            ConstraintsConfig constraintsConfig = null;

            if (column.getType().isAutoIncrement()) {
                columnConfig.setAutoIncrement(true);
            }

            if (column.isNullable() != null && !column.isNullable()) {
                if (constraintsConfig == null) {
                    constraintsConfig = new ConstraintsConfig();
                }

                constraintsConfig.setNullable(false);
            }
//                if (column.isUnique()) {
//					if (constraintsConfig == null) {
//						constraintsConfig = new ConstraintsConfig();
//					}
//					constraintsConfig.setUnique(true);
//				}
            if (constraintsConfig != null) {
                columnConfig.setConstraints(constraintsConfig);
            }

            Object defaultValue = column.getDefaultValue();
            if (defaultValue == null) {
                // do nothing
            } else if (column.getType().isAutoIncrement()) {
                // do nothing
            } else if (defaultValue instanceof Date) {
                columnConfig.setDefaultValueDate((Date) defaultValue);
            } else if (defaultValue instanceof Boolean) {
                columnConfig.setDefaultValueBoolean(((Boolean) defaultValue));
            } else if (defaultValue instanceof Number) {
                columnConfig.setDefaultValueNumeric(((Number) defaultValue));
            } else if (defaultValue instanceof DatabaseFunction) {
                columnConfig.setDefaultValueComputed((DatabaseFunction) defaultValue);
            } else {
                columnConfig.setDefaultValue(defaultValue.toString());
            }

            if (column.getRemarks() != null) {
                columnConfig.setRemarks(column.getRemarks());
            }

            change.addColumn(columnConfig);
            control.setAlreadyHandledMissing(column);
        }


        return new Change[] {
                change
        };
    }
}
