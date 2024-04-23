package liquibase.diff.output.changelog.core;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.core.SetTableRemarksChange;
import liquibase.database.BigqueryDatabase;
import liquibase.database.Database;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

public class BigQueryChangedTableChangeGenerator extends ChangedTableChangeGenerator {

    public BigQueryChangedTableChangeGenerator() {
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        int priority = super.getPriority(objectType, database);
        if (database instanceof BigqueryDatabase) {
            priority += PRIORITY_DATABASE;
        }
        return priority;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
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
            return new Change[]{change};
        } else {
            Difference changedTablespace = differences.getDifference("tablespace");
            if (changedTablespace != null) {
                Scope.getCurrentScope().getLog(this.getClass()).warning("A change of the tablespace was detected, however, Liquibase does not currently generate statements to move a table between tablespaces.");
            }

            return null;
        }
    }


}
