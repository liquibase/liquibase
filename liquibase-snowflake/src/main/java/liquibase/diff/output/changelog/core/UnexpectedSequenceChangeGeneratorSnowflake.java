package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropSequenceChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;

import java.util.Map;

/**
 * Snowflake-specific UnexpectedSequenceChangeGenerator that creates DROP SEQUENCE changes
 * with Snowflake-specific options like CASCADE and RESTRICT for dependency handling.
 */
public class UnexpectedSequenceChangeGeneratorSnowflake extends AbstractChangeGenerator implements UnexpectedObjectChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Sequence.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DEFAULT + PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] { Table.class };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Sequence sequence = (Sequence) unexpectedObject;

        DropSequenceChange change = new DropSequenceChange();
        change.setSequenceName(sequence.getName());
        
        if (control.getIncludeCatalog()) {
            change.setCatalogName(sequence.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(sequence.getSchema().getName());
        }

        // Handle Snowflake-specific sequence attributes
        handleSnowflakeSpecificAttributes(sequence, change);

        return new Change[] { change };
    }

    /**
     * Handles Snowflake-specific sequence attributes for drop operations.
     * Sets CASCADE or RESTRICT options based on sequence namespace attributes.
     */
    private void handleSnowflakeSpecificAttributes(Sequence sequence, DropSequenceChange change) {
        // Check for Snowflake-specific drop options
        String cascade = sequence.getAttribute("cascade", String.class);
        String restrict = sequence.getAttribute("restrict", String.class);
        
        // Store cascade/restrict options in namespace attributes for SQL generator
        if ("true".equalsIgnoreCase(cascade) || "YES".equalsIgnoreCase(cascade)) {
            Map<String, String> attributes = SnowflakeNamespaceAttributeStorage.getAttributes(sequence.getName());
            if (attributes == null) {
                attributes = new java.util.HashMap<>();
            }
            attributes.put("cascade", "true");
            SnowflakeNamespaceAttributeStorage.storeAttributes(sequence.getName(), attributes);
        } else if ("true".equalsIgnoreCase(restrict) || "YES".equalsIgnoreCase(restrict)) {
            Map<String, String> attributes = SnowflakeNamespaceAttributeStorage.getAttributes(sequence.getName());
            if (attributes == null) {
                attributes = new java.util.HashMap<>();
            }
            attributes.put("restrict", "true");
            SnowflakeNamespaceAttributeStorage.storeAttributes(sequence.getName(), attributes);
        }
        
    }
}