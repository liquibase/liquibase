package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateViewChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;

public class MissingViewChangeGenerator implements MissingObjectChangeGenerator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (View.class.isAssignableFrom(objectType)) {
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
        return null;
    }

    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        View view = (View) missingObject;

        CreateViewChange change = new CreateViewChange();
        change.setViewName(view.getName());
        if (control.isIncludeCatalog()) {
            change.setCatalogName(view.getSchema().getCatalog().getName());
        }
        if (control.isIncludeSchema()) {
            change.setSchemaName(view.getSchema().getName());
        }
        String selectQuery = view.getDefinition();
        if (selectQuery == null) {
            selectQuery = "COULD NOT DETERMINE VIEW QUERY";
        }
        change.setSelectQuery(selectQuery);

        return new Change[] { change };

    }
}
