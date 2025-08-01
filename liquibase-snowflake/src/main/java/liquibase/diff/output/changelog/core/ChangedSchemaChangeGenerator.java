package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AlterSchemaChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class ChangedSchemaChangeGenerator implements ChangedObjectChangeGenerator {

    @Override
    public Change[] fixSchema(Change[] changes, CompareControl.SchemaComparison[] schemaComparisons) {
        return changes;
    }

    @Override
    public Change[] fixOutputAsSchema(Change[] changes, CompareControl.SchemaComparison[] schemaComparisons) {
        return changes;
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (liquibase.database.object.Schema.class.isAssignableFrom(objectType) && 
            database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] { Table.class };
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences,
                               DiffOutputControl control, Database referenceDatabase,
                               Database comparisonDatabase, ChangeGeneratorChain chain) {
        
        liquibase.database.object.Schema schema = (liquibase.database.object.Schema) changedObject;
        List<Change> changes = new ArrayList<>();
        
        // Create ALTER SCHEMA change for each difference
        for (Difference difference : differences.getDifferences()) {
            AlterSchemaChange change = new AlterSchemaChange();
            change.setSchemaName(schema.getName());
            
            String field = difference.getField();
            Object newValue = difference.getReferenceValue();
            
            // Map differences to ALTER SCHEMA operations
            switch (field) {
                case "comment":
                    change.setComment((String) newValue);
                    break;
                case "dataRetentionTimeInDays":
                    change.setDataRetentionTimeInDays((String) newValue);
                    break;
                case "maxDataExtensionTimeInDays":
                    change.setNewMaxDataExtensionTimeInDays((String) newValue);
                    break;
                case "defaultDdlCollation":
                    change.setNewDefaultDdlCollation((String) newValue);
                    break;
                case "managedAccess":
                    change.setManagedAccess((Boolean) newValue);
                    break;
                default:
                    // Skip unknown or non-alterable fields
                    continue;
            }
            
            changes.add(change);
        }
        
        return changes.toArray(new Change[0]);
    }
}