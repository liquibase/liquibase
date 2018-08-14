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
import liquibase.util.StringUtil;

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
        if (!((databaseObject1 instanceof Catalog) && (databaseObject2 instanceof Catalog))) {
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

        if (CatalogAndSchema.CatalogAndSchemaCase.ORIGINAL_CASE.equals(accordingTo.getSchemaAndCatalogCase())) {
            if (StringUtil.trimToEmpty(object1Name).equals(StringUtil.trimToEmpty(object2Name))){
                return true;
            }
        } else {
            if (StringUtil.trimToEmpty(object1Name).equalsIgnoreCase(StringUtil.trimToEmpty(object2Name))) {
                return true;
            }
        }
        if (accordingTo.supportsSchemas()) { //no need to check schema mappings
            return false;
        }

        //check with schemaComparisons
        if ((chain.getSchemaComparisons() != null) && (chain.getSchemaComparisons().length > 0)) {
            for (CompareControl.SchemaComparison comparison : chain.getSchemaComparisons()) {
                String comparisonCatalog1;
                String comparisonCatalog2;
                if (accordingTo.supportsSchemas()) {
                    comparisonCatalog1 = comparison.getComparisonSchema().getSchemaName();
                    comparisonCatalog2 = comparison.getReferenceSchema().getSchemaName();
                } else if (accordingTo.supportsCatalogs()) {
                    comparisonCatalog1 = comparison.getComparisonSchema().getCatalogName();
                    comparisonCatalog2 = comparison.getReferenceSchema().getCatalogName();
                } else {
                    break;
                }

                String finalCatalog1 = thisSchema.getCatalogName();
                String finalCatalog2 = otherSchema.getCatalogName();

                if ((comparisonCatalog1 != null) && comparisonCatalog1.equalsIgnoreCase(finalCatalog1)) {
                    finalCatalog1 = comparisonCatalog2;
                } else if ((comparisonCatalog2 != null) && comparisonCatalog2.equalsIgnoreCase(finalCatalog1)) {
                    finalCatalog1 = comparisonCatalog1;
                }

                if (StringUtil.trimToEmpty(finalCatalog1).equalsIgnoreCase(StringUtil.trimToEmpty(finalCatalog2))) {
                    return true;
                }

                if ((comparisonCatalog1 != null) && comparisonCatalog1.equalsIgnoreCase(finalCatalog2)) {
                    finalCatalog2 = comparisonCatalog2;
                } else if ((comparisonCatalog2 != null) && comparisonCatalog2.equalsIgnoreCase(finalCatalog2)) {
                    finalCatalog2 = comparisonCatalog1;
                }

                if (StringUtil.trimToEmpty(finalCatalog1).equalsIgnoreCase(StringUtil.trimToEmpty(finalCatalog2))) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude) {
        ObjectDifferences differences = new ObjectDifferences(compareControl);
        differences.compare("name", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Schema.class, accordingTo));

        return differences;
    }
}
