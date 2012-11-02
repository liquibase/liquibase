package liquibase.structurecompare.core;

import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.structure.DatabaseObject;
import liquibase.structurecompare.DatabaseObjectComparator;
import liquibase.structurecompare.DatabaseObjectComparatorChain;

public class DefaultDatabaseObjectComparator implements DatabaseObjectComparator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        return PRIORITY_DEFAULT;
    }

    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        String object1Name = accordingTo.correctObjectName(databaseObject1.getName(), databaseObject1.getClass());
        String object2Name = accordingTo.correctObjectName(databaseObject2.getName(), databaseObject2.getClass());

        if (object1Name == null) {
            return object2Name == null;
        }
        return object1Name.equals(object2Name);
    }

    public DiffResult compare(DatabaseObject databaseObject, Database accordingTo) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
