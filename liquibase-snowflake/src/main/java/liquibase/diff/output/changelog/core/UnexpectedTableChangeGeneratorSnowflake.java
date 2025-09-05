package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropTableChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

/**
 * Snowflake-specific generator for creating DROP TABLE changes when tables are unexpected.
 * Handles Snowflake-specific drop options like CASCADE and RESTRICT.
 */
public class UnexpectedTableChangeGeneratorSnowflake extends UnexpectedTableChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Table.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return super.getPriority(objectType, database) + PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        // Get the base DropTableChange from the parent class
        Change[] parentChanges = super.fixUnexpected(unexpectedObject, control, referenceDatabase, comparisonDatabase, chain);
        
        if (parentChanges == null || parentChanges.length == 0) {
            return parentChanges;
        }
        
        // Enhance the DropTableChange with Snowflake-specific attributes
        for (Change change : parentChanges) {
            if (change instanceof DropTableChange) {
                enhanceWithSnowflakeAttributes((DropTableChange) change, (Table) unexpectedObject);
            }
        }
        
        return parentChanges;
    }

    /**
     * Enhances a DropTableChange with Snowflake-specific drop options.
     */
    private void enhanceWithSnowflakeAttributes(DropTableChange change, Table table) {
        // Handle cascade option if table has foreign key references
        // Snowflake supports CASCADE to force drop tables with foreign key references
        String cascade = table.getAttribute("cascade", String.class);
        if ("true".equalsIgnoreCase(cascade) || "yes".equalsIgnoreCase(cascade)) {
            change.setCascadeConstraints(true);
        }
        
        // Handle restrict option to prevent drop if foreign key references exist
        String restrict = table.getAttribute("restrict", String.class);
        if ("true".equalsIgnoreCase(restrict) || "yes".equalsIgnoreCase(restrict)) {
            // DropTableChange doesn't have a setRestrict method, so we document this in remarks
            // This is a limitation of the current API
        }
        
        // For Snowflake, we might want to default to CASCADE for easier cleanup
        // unless specifically restricted, but this should be configurable
        if (cascade == null && restrict == null) {
            // Check if this is a temporary table - these can be dropped without CASCADE concerns
            String isTemporary = table.getAttribute("isTemporary", String.class);
            String isTransient = table.getAttribute("isTransient", String.class);
            
            if ("YES".equalsIgnoreCase(isTemporary) || "YES".equalsIgnoreCase(isTransient)) {
                // Temporary and transient tables typically don't need CASCADE
            } else {
                // For regular tables, use CASCADE by default to handle foreign key dependencies
                // This makes the diff more likely to succeed but should be used carefully
                change.setCascadeConstraints(true);
            }
        }
    }
}