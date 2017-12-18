package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.SetTableRemarksChange;
import liquibase.database.Database;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

public class ChangedTableChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
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
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, final Database comparisonDatabase, ChangeGeneratorChain chain) {
        Table table = (Table) changedObject;

        Difference changedRemarks = differences.getDifference("remarks");
        if (changedRemarks != null) {
            SetTableRemarksChange change = new SetTableRemarksChange();
            if (control.getIncludeCatalog()) {
                change.setCatalogName(table.getSchema().getCatalogName());
            }
            if (control.getIncludeSchema()) {
                change.setSchemaName(table.getSchema().getName());
            }

            change.setTableName(table.getName());
            change.setRemarks(table.getRemarks());
            
            return new Change[] {
                    change
            };
        }

        Difference changedTablespace = differences.getDifference("tablespace");
        
        if (changedTablespace != null) {
            // TODO: Implement moveTableToDifferentTablespace change type!
            LogService.getLog(getClass()).warning(LogType.LOG, "A change of the tablespace was detected, however, the change " +
             "type to move a table from tablespace A to tablespace B has not been implemented yet. Sorry.");
        }

        return null;
    }
}
