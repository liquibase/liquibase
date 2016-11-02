package liquibase.diff.output.changelog.core;

import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.DropIndexChange;
import liquibase.database.Database;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangeGeneratorFactory;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChangedIndexChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Index.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        //don't try to recreate indexes that differ in just clustered
        Difference clusteredDiff = differences.getDifference("clustered");
        if (clusteredDiff != null) {
            if (clusteredDiff.getReferenceValue() == null || clusteredDiff.getComparedValue() == null) {
                differences.removeDifference("clustered");
            }
        }
        if (!differences.hasDifferences()) {
            return new Change[0];
        }

        Index index = (Index) changedObject;

        if (index.getTable() != null) {
            if (index.getTable().getPrimaryKey() != null && DatabaseObjectComparatorFactory.getInstance().isSameObject(index.getTable().getPrimaryKey().getBackingIndex(), changedObject, differences.getSchemaComparisons(), comparisonDatabase)) {
                return ChangeGeneratorFactory.getInstance().fixChanged(index.getTable().getPrimaryKey(), differences, control, referenceDatabase, comparisonDatabase);
            }

            List<UniqueConstraint> uniqueConstraints = index.getTable().getUniqueConstraints();
            if (uniqueConstraints != null) {
                for (UniqueConstraint constraint : uniqueConstraints) {
                    if (constraint.getBackingIndex() != null && DatabaseObjectComparatorFactory.getInstance().isSameObject(constraint.getBackingIndex(), changedObject,differences.getSchemaComparisons(), comparisonDatabase)) {
                        return ChangeGeneratorFactory.getInstance().fixChanged(constraint, differences, control, referenceDatabase, comparisonDatabase);
                    }

                }
            }
        }

        DropIndexChange dropIndexChange = createDropIndexChange();
        dropIndexChange.setTableName(index.getTable().getName());
        dropIndexChange.setIndexName(index.getName());
        
        CreateIndexChange addIndexChange = createCreateIndexChange();
        addIndexChange.setTableName(index.getTable().getName());
        List<AddColumnConfig> columns = new ArrayList<AddColumnConfig>();
        for (Column col : index.getColumns()) {
            columns.add(new AddColumnConfig(col));
        }
        addIndexChange.setColumns(columns);
        addIndexChange.setIndexName(index.getName());
        addIndexChange.setUnique(index.isUnique());

        if (control.getIncludeCatalog()) {
            dropIndexChange.setCatalogName(index.getSchema().getCatalogName());
            addIndexChange.setCatalogName(index.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            dropIndexChange.setSchemaName(index.getSchema().getName());
            addIndexChange.setSchemaName(index.getSchema().getName());
        }

        Difference columnsDifference = differences.getDifference("columns");
        
        if (columnsDifference != null) {
            List<Column> referenceColumns = (List<Column>) columnsDifference.getReferenceValue();
            List<Column> comparedColumns = (List<Column>) columnsDifference.getComparedValue();

            StringUtils.StringUtilsFormatter<Column> formatter = new StringUtils.StringUtilsFormatter<Column>() {
                @Override
                public String toString(Column obj) {
                    return obj.toString(false);
                }
            };

            control.setAlreadyHandledChanged(new Index().setTable(index.getTable()).setColumns(referenceColumns));
            if (!StringUtils.join(referenceColumns, ",", formatter).equalsIgnoreCase(StringUtils.join(comparedColumns, ",", formatter))) {
                control.setAlreadyHandledChanged(new Index().setTable(index.getTable()).setColumns(comparedColumns));
            }
    
            if (index.isUnique() != null && index.isUnique()) {
                control.setAlreadyHandledChanged(new UniqueConstraint().setTable(index.getTable()).setColumns(referenceColumns));
                if (!StringUtils.join(referenceColumns, ",", formatter).equalsIgnoreCase(StringUtils.join(comparedColumns, ",", formatter))) {
                    control.setAlreadyHandledChanged(new UniqueConstraint().setTable(index.getTable()).setColumns(comparedColumns));
                }
            }
        }

        return new Change[] { dropIndexChange, addIndexChange };
    }

    protected DropIndexChange createDropIndexChange() {
        return new DropIndexChange();
    }

    protected CreateIndexChange createCreateIndexChange() {
        return new CreateIndexChange();
    }
}
