package liquibase.diff.compare.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtils;

import java.util.Set;

public class CatalogComparator implements DatabaseObjectComparator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Catalog.class.isAssignableFrom(objectType)) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain) {
       return null;
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!(databaseObject1 instanceof Catalog && databaseObject2 instanceof Catalog)) {
            return false;
        }

        if (!accordingTo.supportsCatalogs()) {
            return true;
        }

        String object1Name;
        if (((Catalog) databaseObject1).isDefault()) {
            object1Name = null;
        } else {
            object1Name = databaseObject1.getName();
        }

        String object2Name;
        if (((Catalog) databaseObject2).isDefault()) {
            object2Name = null;
        } else {
            object2Name = databaseObject2.getName();
        }

        CatalogAndSchema thisSchema = new CatalogAndSchema(object1Name, null).standardize(accordingTo);
        CatalogAndSchema otherSchema = new CatalogAndSchema(object2Name, null).standardize(accordingTo);

        if (thisSchema.getCatalogName() == null) {
            return otherSchema.getCatalogName() == null;
        }

        return thisSchema.getCatalogName().equalsIgnoreCase(otherSchema.getCatalogName());
    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude) {
        ObjectDifferences differences = new ObjectDifferences(compareControl);
        differences.compare("name", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Schema.class, accordingTo));

        return differences;
    }
}
