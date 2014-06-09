package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.change.core.DropPrimaryKeyChange;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.UniqueConstraint;
import liquibase.structure.core.FulltextConstraint;
import liquibase.util.StringUtils;

import java.util.Collection;

public class ChangedPrimaryKeyChangeGenerator  implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {Index.class, UniqueConstraint.class, FulltextConstraint.class };
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        PrimaryKey pk = (PrimaryKey) changedObject;

        DropPrimaryKeyChange dropPkChange = new DropPrimaryKeyChange();
        dropPkChange.setTableName(pk.getTable().getName());

        AddPrimaryKeyChange addPkChange = new AddPrimaryKeyChange();
        addPkChange.setTableName(pk.getTable().getName());
        addPkChange.setColumnNames(pk.getColumnNames());
        addPkChange.setConstraintName(pk.getName());


        if (control.getIncludeCatalog()) {
            dropPkChange.setCatalogName(pk.getSchema().getCatalogName());
            addPkChange.setCatalogName(pk.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            dropPkChange.setSchemaName(pk.getSchema().getName());
            addPkChange.setSchemaName(pk.getSchema().getName());
        }

        String referenceColumns = StringUtils.join((Collection<String>) differences.getDifference("columnNames").getReferenceValue(), ",");
        String comparedColumns = StringUtils.join((Collection<String>) differences.getDifference("columnNames").getComparedValue(), ",");

        control.setAlreadyHandledChanged(new Index().setTable(pk.getTable()).setColumns(referenceColumns));
        if (!referenceColumns.equalsIgnoreCase(comparedColumns)) {
            control.setAlreadyHandledChanged(new Index().setTable(pk.getTable()).setColumns(comparedColumns));
        }

        control.setAlreadyHandledChanged(new UniqueConstraint().setTable(pk.getTable()).setColumns(referenceColumns));
        if (!referenceColumns.equalsIgnoreCase(comparedColumns)) {
            control.setAlreadyHandledChanged(new UniqueConstraint().setTable(pk.getTable()).setColumns(comparedColumns));
        }
        
        control.setAlreadyHandledChanged(new FulltextConstraint().setTable(pk.getTable()).setColumns(referenceColumns));
        if (!referenceColumns.equalsIgnoreCase(comparedColumns)) {
            control.setAlreadyHandledChanged(new FulltextConstraint().setTable(pk.getTable()).setColumns(comparedColumns));
        }

        return new Change[] { dropPkChange, addPkChange };
    }
}
