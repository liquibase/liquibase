package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropViewChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.View;

public class UnexpectedViewChangeGenerator implements UnexpectedObjectChangeGenerator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (View.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain) {
        View view = (View) unexpectedObject;

        DropViewChange change = new DropViewChange();
        change.setViewName(view.getName());
        if (control.isIncludeCatalog()) {
            change.setCatalogName(view.getSchema().getCatalog().getName());
        }
        if (control.isIncludeSchema()) {
            change.setSchemaName(view.getSchema().getName());
        }

        return new Change[]{change};


    }
}
