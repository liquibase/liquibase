package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.SetColumnRemarksChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.View;

public class MissingViewColumnRemarkChangeGenerator extends AbstractChangeGenerator implements MissingObjectChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Column.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT + 1;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                View.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Column column = (Column) missingObject;

        if (column.getRelation() instanceof View) {
            if (column.getRemarks() != null) {
                SetColumnRemarksChange columnRemarks = new SetColumnRemarksChange();
                columnRemarks.setColumnName(column.getName());
                columnRemarks.setColumnDataType(column.getType().getTypeName());
                columnRemarks.setRemarks(column.getRemarks());
                columnRemarks.setCatalogName(column.getRelation().getSchema().getCatalogName());
                columnRemarks.setSchemaName(column.getRelation().getSchema().getName());
                columnRemarks.setTableName(column.getRelation().getName());
                return new Change[] { columnRemarks };
            }
        }
        return null;
    }
}
