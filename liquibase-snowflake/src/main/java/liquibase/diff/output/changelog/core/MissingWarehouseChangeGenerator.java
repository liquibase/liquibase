package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateWarehouseChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.Warehouse;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;

public class MissingWarehouseChangeGenerator implements MissingObjectChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Warehouse.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, 
                             Database referenceDatabase, Database comparisonDatabase, 
                             ChangeGeneratorChain chain) {
        Warehouse warehouse = (Warehouse) missingObject;
        
        CreateWarehouseChange change = new CreateWarehouseChange();
        change.setWarehouseName(warehouse.getName());
        
        // Map all configuration properties (exclude state properties)
        if (warehouse.getType() != null) {
            change.setWarehouseType(warehouse.getType());
        }
        if (warehouse.getSize() != null) {
            change.setWarehouseSize(warehouse.getSize());
        }
        if (warehouse.getMinClusterCount() != null) {
            change.setMinClusterCount(warehouse.getMinClusterCount());
        }
        if (warehouse.getMaxClusterCount() != null) {
            change.setMaxClusterCount(warehouse.getMaxClusterCount());
        }
        if (warehouse.getAutoSuspend() != null) {
            change.setAutoSuspend(warehouse.getAutoSuspend());
        }
        if (warehouse.getAutoResume() != null) {
            change.setAutoResume(warehouse.getAutoResume());
        }
        if (warehouse.getResourceMonitor() != null) {
            change.setResourceMonitor(warehouse.getResourceMonitor());
        }
        if (warehouse.getComment() != null) {
            change.setComment(warehouse.getComment());
        }
        if (warehouse.getEnableQueryAcceleration() != null) {
            change.setEnableQueryAcceleration(warehouse.getEnableQueryAcceleration());
        }
        if (warehouse.getQueryAccelerationMaxScaleFactor() != null) {
            change.setQueryAccelerationMaxScaleFactor(warehouse.getQueryAccelerationMaxScaleFactor());
        }
        if (warehouse.getScalingPolicy() != null) {
            change.setScalingPolicy(warehouse.getScalingPolicy());
        }
        if (warehouse.getResourceConstraint() != null) {
            change.setResourceConstraint(warehouse.getResourceConstraint());
        }
        
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