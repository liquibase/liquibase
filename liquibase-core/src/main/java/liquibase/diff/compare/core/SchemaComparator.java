package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;

public class SchemaComparator implements DatabaseObjectComparator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Schema.class.isAssignableFrom(objectType)) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }

    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (chain.isSameObject(databaseObject1, databaseObject2, accordingTo)) {
            return true;
        }

        if (!(databaseObject1 instanceof Schema && databaseObject2 instanceof Schema)) {
            return false;
        }

        Schema thisSchema = (Schema) databaseObject1;
        Schema otherSchema = (Schema) databaseObject2;

        if (accordingTo.supportsCatalogs()) {
            if (thisSchema.getCatalogName() == null) {
                return otherSchema.getCatalogName() == null || accordingTo.getDefaultCatalogName() == null || accordingTo.getDefaultCatalogName().equals(otherSchema.getCatalogName());
            }
            if (!thisSchema.getCatalogName().equals(otherSchema.getCatalogName())) {
                return false;
            }
        }
        if (accordingTo.supportsSchemas()) {
            return thisSchema.getName().equals(otherSchema.getName());
        }
        return true;
    }


    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo);
        return differences;
    }
}
