package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateDatabaseChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

public class MissingDatabaseChangeGenerator implements MissingObjectChangeGenerator {

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
        if (liquibase.database.object.Database.class.isAssignableFrom(objectType) && 
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
        return new Class[] { Schema.class, Table.class };
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, 
                               Database referenceDatabase, Database comparisonDatabase, 
                               ChangeGeneratorChain chain) {
        
        liquibase.database.object.Database database = (liquibase.database.object.Database) missingObject;
        
        CreateDatabaseChange change = new CreateDatabaseChange();
        change.setDatabaseName(database.getName());
        
        // Set configuration properties if they exist
        if (database.getComment() != null) {
            change.setComment(database.getComment());
        }
        if (database.getDataRetentionTimeInDays() != null) {
            change.setDataRetentionTimeInDays(database.getDataRetentionTimeInDays());
        }
        if (database.getMaxDataExtensionTimeInDays() != null) {
            change.setMaxDataExtensionTimeInDays(database.getMaxDataExtensionTimeInDays());
        }
        if (database.getTransient() != null && database.getTransient()) {
            change.setTransient(database.getTransient());
        }
        if (database.getDefaultDdlCollation() != null) {
            change.setDefaultDdlCollation(database.getDefaultDdlCollation());
        }
        // ResourceMonitor is not available in CreateDatabaseChange - skip for now
        // if (database.getResourceMonitor() != null) {
        //     change.setResourceMonitor(database.getResourceMonitor());
        // }
        
        return new Change[] { change };
    }
}