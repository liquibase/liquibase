package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;

public class UnexpectedForeignKeyChangeGenerator implements UnexpectedObjectChangeGenerator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (ForeignKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        ForeignKey fk = (ForeignKey) unexpectedObject;

        DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
        change.setConstraintName(fk.getName());
        change.setBaseTableName(fk.getForeignKeyTable().getName());
        if (control.isIncludeCatalog()) {
            change.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());
        }
        if (control.isIncludeSchema()) {
            change.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getName());
        }

        return new Change[] { change };

    }
}
