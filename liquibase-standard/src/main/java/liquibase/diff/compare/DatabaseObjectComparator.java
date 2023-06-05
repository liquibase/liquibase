package liquibase.diff.compare;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.structure.DatabaseObject;

import java.util.Set;

public interface DatabaseObjectComparator {

    int PRIORITY_NONE = -1;
    int PRIORITY_DEFAULT = 1;
    int PRIORITY_TYPE = 5;
    int PRIORITY_DATABASE = 10;

    int getPriority(Class<? extends DatabaseObject> objectType, Database database);

    boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain);

    String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain);

    ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude);
}
