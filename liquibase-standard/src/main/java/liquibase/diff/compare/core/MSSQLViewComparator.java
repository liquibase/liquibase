package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.View;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * MSSQL-specific comparator for View objects that normalizes view definitions
 * before comparison to prevent false diffs caused by inconsistent schema
 * qualification across MSSQL server versions.
 * <p>
 * MSSQL's OBJECT_DEFINITION() returns different formats depending on server version:
 * <ul>
 *   <li>Some versions: {@code CREATE VIEW [dbo].[view_demo] WITH SCHEMABINDING AS ...}</li>
 *   <li>Other versions: {@code CREATE VIEW view_demo WITH SCHEMABINDING AS ...}</li>
 * </ul>
 * <p>
 * This comparator strips the optional {@code [schema].} prefix and bracket quoting
 * from the {@code CREATE VIEW} header <em>only for comparison purposes</em>,
 * leaving the original definition on the snapshot object intact for changelog generation.
 */
public class MSSQLViewComparator implements DatabaseObjectComparator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof MSSQLDatabase && View.class.isAssignableFrom(objectType)) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2,
                                Database accordingTo, DatabaseObjectComparatorChain chain) {
        return chain.isSameObject(databaseObject1, databaseObject2, accordingTo);
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo,
                         DatabaseObjectComparatorChain chain) {
        return chain.hash(databaseObject, accordingTo);
    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2,
                                             Database accordingTo, CompareControl compareControl,
                                             DatabaseObjectComparatorChain chain, Set<String> exclude) {
        exclude.add("definition");

        ObjectDifferences differences = chain.findDifferences(
                databaseObject1, databaseObject2, accordingTo, compareControl, exclude);

        String def1 = ((View) databaseObject1).getDefinition();
        String def2 = ((View) databaseObject2).getDefinition();

        if (def1 == null && def2 == null) {
            return differences;
        }
        if (def1 == null || def2 == null) {
            differences.addDifference("definition", def1, def2);
            return differences;
        }

        String schemaName1 = databaseObject1.getSchema() != null ? databaseObject1.getSchema().getName() : null;
        String schemaName2 = databaseObject2.getSchema() != null ? databaseObject2.getSchema().getName() : null;

        String normalized1 = normalizeViewDefinition(def1, schemaName1);
        String normalized2 = normalizeViewDefinition(def2, schemaName2);

        if (!normalized1.equals(normalized2)) {
            differences.addDifference("definition", def1, def2);
        }

        return differences;
    }

    /**
     * Normalizes an MSSQL view definition for comparison by stripping the
     * optional {@code [schema].} prefix and bracket quoting from the
     * {@code CREATE VIEW} header.
     * <p>
     * This does NOT modify the snapshot object — it only creates a
     * normalized copy for comparison purposes.
     *
     * @param definition the raw view definition from OBJECT_DEFINITION()
     * @param schemaName the schema name to strip, or null if unknown
     * @return the normalized definition string
     */
    static String normalizeViewDefinition(String definition, String schemaName) {
        if (definition == null) {
            return null;
        }

        String result = definition;

        // Pass 1: strip "[schema]." prefix when present
        if (schemaName != null) {
            result = result.replaceFirst("(?i)(create\\s+view\\s+)\\[?"
                    + Pattern.quote(schemaName)
                    + "\\]?\\.", "$1");
        }

        // Pass 2: strip brackets from the view name itself
        result = result.replaceFirst(
                "(?i)(create\\s+view\\s+)\\[([^\\]\\s]+)\\]", "$1$2");

        return result;
    }
}
