package liquibase.diff.compare.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtils;

import java.util.Set;

public class SchemaComparator implements DatabaseObjectComparator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Schema.class.isAssignableFrom(objectType)) {
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
        if (!(databaseObject1 instanceof Schema && databaseObject2 instanceof Schema)) {
            return false;
        }

        CatalogAndSchema thisSchema = ((Schema) databaseObject1).toCatalogAndSchema().standardize(accordingTo);
        CatalogAndSchema otherSchema = ((Schema) databaseObject2).toCatalogAndSchema().standardize(accordingTo);

        if (accordingTo.supportsSchemas()) {
            return StringUtils.trimToEmpty(thisSchema.getSchemaName()).equalsIgnoreCase(StringUtils.trimToEmpty(otherSchema.getSchemaName()));
        } else if (accordingTo.supportsCatalogs()) {
            return StringUtils.trimToEmpty(thisSchema.getCatalogName()).equalsIgnoreCase(StringUtils.trimToEmpty(otherSchema.getCatalogName()));
        } else {
            return true;
        }
    }


    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude) {
        ObjectDifferences differences = new ObjectDifferences(compareControl);
        differences.compare("name", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Schema.class, accordingTo));

        return differences;
    }
}
