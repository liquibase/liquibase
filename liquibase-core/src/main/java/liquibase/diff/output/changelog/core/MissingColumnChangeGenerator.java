package liquibase.diff.output.changelog.core;

import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;

public class MissingColumnChangeGenerator implements MissingObjectChangeGenerator {

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
        return new Class[] { PrimaryKey.class };
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Column column = (Column) missingObject;
//        if (!shouldModifyColumn(column)) {
//            continue;
//        }

        if (column.getRelation() instanceof View) {
            return null;
        }

        AddColumnChange change = new AddColumnChange();
        change.setTableName(column.getRelation().getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(column.getRelation().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(column.getRelation().getSchema().getName());
        }

        AddColumnConfig columnConfig = new AddColumnConfig();
        columnConfig.setName(column.getName());

        String dataType = column.getType().toString();

        columnConfig.setType(dataType);

        Object defaultValue = column.getDefaultValue();
        MissingTableChangeGenerator.setDefaultValue(columnConfig, column, comparisonDatabase);
        if (defaultValue != null) {
            String defaultValueString = null;
            try {
                defaultValueString = DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase).objectToSql(defaultValue, referenceDatabase);
            } catch (NullPointerException e) {
                throw e;
            }
            if (defaultValueString != null) {
                defaultValueString = defaultValueString.replaceFirst("'",
                        "").replaceAll("'$", "");
            }
            columnConfig.setDefaultValue(defaultValueString);
        }

        if (column.getRemarks() != null) {
            columnConfig.setRemarks(column.getRemarks());
        }
        ConstraintsConfig constraintsConfig = columnConfig.getConstraints();
        if (column.isNullable() != null && !column.isNullable()) {
            if (constraintsConfig == null) {
                constraintsConfig = new ConstraintsConfig();
            }
            constraintsConfig.setNullable(false);
        }
        if (constraintsConfig != null) {
            columnConfig.setConstraints(constraintsConfig);
        }

        change.addColumn(columnConfig);

        return new Change[] { change };
    }
}
