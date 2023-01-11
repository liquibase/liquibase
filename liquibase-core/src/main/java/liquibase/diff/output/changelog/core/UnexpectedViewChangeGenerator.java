package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropViewChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;

public class UnexpectedViewChangeGenerator extends AbstractChangeGenerator implements UnexpectedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (View.class.isAssignableFrom(objectType)) {
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
        return new Class[] {
                Table.class,
                Column.class
        };
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        View view = (View) unexpectedObject;

        DropViewChange change = new DropViewChange();
        change.setViewName(view.getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(view.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(view.getSchema().getName());
        }

        for (Column column : view.getColumns()) {
            control.setAlreadyHandledUnexpected(column);
        };


        return new Change[]{change};


    }
}
