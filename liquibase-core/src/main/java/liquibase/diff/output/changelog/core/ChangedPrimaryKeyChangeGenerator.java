package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.change.core.DropPrimaryKeyChange;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangeGeneratorFactory;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChangedPrimaryKeyChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {Index.class, UniqueConstraint.class };
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {

        //don't try to recreate PKs that differ in just clustered
        Difference clusteredDiff = differences.getDifference("clustered");
        if (clusteredDiff != null) {
            if ((clusteredDiff.getReferenceValue() == null) || (clusteredDiff.getComparedValue() == null)) {
                differences.removeDifference("clustered");
            }
        }
        if (!differences.hasDifferences()) {
            return new Change[0];
        }

        PrimaryKey pk = (PrimaryKey) changedObject;

        List<Change> returnList = new ArrayList<>();


        DropPrimaryKeyChange dropPkChange = new DropPrimaryKeyChange();
        dropPkChange.setTableName(pk.getTable().getName());
        returnList.add(dropPkChange);

        AddPrimaryKeyChange addPkChange = new AddPrimaryKeyChange();
        addPkChange.setTableName(pk.getTable().getName());
        addPkChange.setColumnNames(pk.getColumnNames());
        addPkChange.setConstraintName(pk.getName());

        if (comparisonDatabase instanceof OracleDatabase) {
            Index backingIndex = pk.getBackingIndex();
            if ((backingIndex != null) && (backingIndex.getName() != null)) {
                Change[] indexChanges = ChangeGeneratorFactory.getInstance().fixMissing(backingIndex, control, referenceDatabase, comparisonDatabase);
                if (indexChanges != null) {
                    returnList.addAll(Arrays.asList(indexChanges));
                }

                addPkChange.setForIndexName(backingIndex.getName());
                Schema schema = backingIndex.getSchema();
                if (schema != null) {
                    if (control.getIncludeCatalog()) {
                        addPkChange.setForIndexCatalogName(schema.getCatalogName());
                    }
                    if (control.getIncludeSchema()) {
                        addPkChange.setForIndexSchemaName(schema.getName());
                    }
                }
            }
        }
        returnList.add(addPkChange);

        if (control.getIncludeCatalog()) {
            dropPkChange.setCatalogName(pk.getSchema().getCatalogName());
            addPkChange.setCatalogName(pk.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            dropPkChange.setSchemaName(pk.getSchema().getName());
            addPkChange.setSchemaName(pk.getSchema().getName());
        }

        Difference columnDifferences = differences.getDifference("columns");
        List<Column> referenceColumns;
        List<Column> comparedColumns;
        if (columnDifferences == null) {
            referenceColumns = pk.getColumns();
            comparedColumns = pk.getColumns();
        } else {
            referenceColumns = (List<Column>) columnDifferences.getReferenceValue();
            comparedColumns = (List<Column>) columnDifferences.getComparedValue();
        }

        StringUtils.ToStringFormatter formatter = new StringUtils.ToStringFormatter();

        control.setAlreadyHandledChanged(new Index().setTable(pk.getTable()).setColumns(referenceColumns));
        if (!StringUtils.join(referenceColumns, ",", formatter).equalsIgnoreCase(StringUtils.join(comparedColumns, ",", formatter))) {
            control.setAlreadyHandledChanged(new Index().setTable(pk.getTable()).setColumns(comparedColumns));
        }

        control.setAlreadyHandledChanged(new UniqueConstraint().setTable(pk.getTable()).setColumns(referenceColumns));
        if (!StringUtils.join(referenceColumns, ",", formatter).equalsIgnoreCase(StringUtils.join(comparedColumns, "," , formatter))) {
            control.setAlreadyHandledChanged(new UniqueConstraint().setTable(pk.getTable()).setColumns(comparedColumns));
        }

        return returnList.toArray(new Change[returnList.size()]);
    }
}
