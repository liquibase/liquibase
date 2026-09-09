package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Grant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Establishes identity for {@link Grant} objects using the tuple of (privilege, object type, object name, grantee)
 * rather than the synthesized {@link Grant#getName()}, so that a change to just the "WITH GRANT OPTION" flag is
 * reported as a "changed" grant instead of a missing/unexpected pair.
 */
public class GrantComparator implements DatabaseObjectComparator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Grant.class.isAssignableFrom(objectType)) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain) {
        Grant grant = (Grant) databaseObject;

        List<String> hash = new ArrayList<>();
        hash.add(lower(grant.getPrivilege()));
        hash.add(lower(grant.getObjectType()));
        hash.add(lower(grant.getObjectName()));
        hash.add(lower(grant.getGranteeName()));
        if (grant.getSchema() != null) {
            hash.addAll(Arrays.asList(DatabaseObjectComparatorFactory.getInstance().hash(grant.getSchema(), chain.getSchemaComparisons(), accordingTo)));
        }
        return hash.toArray(new String[0]);
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!((databaseObject1 instanceof Grant) && (databaseObject2 instanceof Grant))) {
            return false;
        }

        Grant thisGrant = (Grant) databaseObject1;
        Grant otherGrant = (Grant) databaseObject2;

        if ((thisGrant.getSchema() != null) && (otherGrant.getSchema() != null) &&
                !DatabaseObjectComparatorFactory.getInstance().isSameObject(thisGrant.getSchema(), otherGrant.getSchema(), chain.getSchemaComparisons(), accordingTo)) {
            return false;
        }

        return namesMatch(thisGrant.getPrivilege(), otherGrant.getPrivilege(), accordingTo)
                && namesMatch(thisGrant.getObjectType(), otherGrant.getObjectType(), accordingTo)
                && namesMatch(thisGrant.getObjectName(), otherGrant.getObjectName(), accordingTo)
                && namesMatch(thisGrant.getGranteeName(), otherGrant.getGranteeName(), accordingTo);
    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude) {
        //identity fields already established via isSameObject/hash; only the remaining attributes (e.g. grantable) are real "changes"
        exclude.add("name");
        exclude.add("objectType");
        exclude.add("objectName");
        exclude.add("privilege");
        exclude.add("granteeName");

        return chain.findDifferences(databaseObject1, databaseObject2, accordingTo, compareControl, exclude);
    }

    private static boolean namesMatch(String name1, String name2, Database accordingTo) {
        return DefaultDatabaseObjectComparator.compareObjectNames(accordingTo, name1, name2);
    }

    private static String lower(String value) {
        return value == null ? null : value.toLowerCase();
    }
}
