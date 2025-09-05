package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropWarehouseChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.Warehouse;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;

public class UnexpectedWarehouseChangeGenerator implements UnexpectedObjectChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Warehouse.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, 
                                Database referenceDatabase, Database comparisonDatabase, 
                                ChangeGeneratorChain chain) {
        Warehouse warehouse = (Warehouse) unexpectedObject;
        
        DropWarehouseChange change = new DropWarehouseChange();
        change.setWarehouseName(warehouse.getName());
        
        return new Change[] { change };
    }

    @Override
    public Change[] fixSchema(Change[] changes, liquibase.diff.compare.CompareControl.SchemaComparison[] schemaComparisons) {
        return changes;
    }

    @Override
    public Change[] fixOutputAsSchema(Change[] changes, liquibase.diff.compare.CompareControl.SchemaComparison[] schemaComparisons) {
        return changes;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }
}