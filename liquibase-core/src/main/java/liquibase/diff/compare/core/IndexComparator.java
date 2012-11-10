package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;

public class IndexComparator implements DatabaseObjectComparator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Index.class.isAssignableFrom(objectType)) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }

    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (chain.isSameObject(databaseObject1, databaseObject2, accordingTo)) {
            return true;
        }

        Index thisIndex = (Index) databaseObject1;
        Index otherIndex = (Index) databaseObject2;

        if (thisIndex.getColumns().size() == 0 || otherIndex.getColumns().size() == 0) {
            return false;
        }

        if (!DatabaseObjectComparatorFactory.getInstance().isSameObject(thisIndex.getTable(), otherIndex.getTable(), accordingTo)) {
            return false;
        }


        if (thisIndex.getColumns().size() !=  otherIndex.getColumns().size()) {
            return false;
        }

        for (int i=0; i<otherIndex.getColumns().size(); i++) {
            if (! DatabaseObjectComparatorFactory.getInstance().isSameObject(new Column().setName(thisIndex.getColumns().get(i)), new Column().setName(otherIndex.getColumns().get(i)), accordingTo)) {
                return false;
            }
        }
        return true;
    }


    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo);
        return differences;
    }
}
