package liquibase.structurecompare.core;

import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structurecompare.DatabaseObjectComparator;
import liquibase.structurecompare.DatabaseObjectComparatorChain;
import liquibase.structurecompare.DatabaseObjectComparatorFactory;

public class IndexComparator implements DatabaseObjectComparator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Index.class.isAssignableFrom(objectType)) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }

    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        Index thisIndex = (Index) databaseObject1;
        Index otherIndex = (Index) databaseObject2;

        if (otherIndex.getColumns().size() == 0) {
            return chain.isSameObject(databaseObject1, databaseObject2, accordingTo);
        }

        if (thisIndex.getTable() != null) {
            return DatabaseObjectComparatorFactory.getInstance().isSameObject(thisIndex.getTable(), otherIndex.getTable(), accordingTo);
        }

        if (otherIndex.getColumns() != null & otherIndex.getColumns().size() > 0) {
            for (int i=0; i<otherIndex.getColumns().size(); i++) {
                if (! DatabaseObjectComparatorFactory.getInstance().isSameObject(new Column().setName(thisIndex.getColumns().get(i)), new Column().setName(otherIndex.getColumns().get(i)), accordingTo)) {
                    return false;
                }
            }
            return true;
        }

        throw new UnexpectedLiquibaseException("Nothing to compare");
    }

    public boolean containsDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        return chain.containsDifferences(databaseObject1, databaseObject2, accordingTo);
    }
}
