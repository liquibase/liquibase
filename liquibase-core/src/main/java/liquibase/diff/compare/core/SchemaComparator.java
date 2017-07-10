package liquibase.diff.compare.core;

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
        if (!((databaseObject1 instanceof Schema) && (databaseObject2 instanceof Schema))) {
            return false;
        }

        String schemaName1 = null;
        String schemaName2 = null;

        if (accordingTo.supportsSchemas()) {
            schemaName1 = databaseObject1.getName();
            schemaName2 = databaseObject2.getName();

        } else if (accordingTo.supportsCatalogs()) {
            schemaName1 = ((Schema) databaseObject1).getCatalogName();
            schemaName2 = ((Schema) databaseObject2).getCatalogName();
        }


        if (StringUtils.trimToEmpty(schemaName1).equalsIgnoreCase(StringUtils.trimToEmpty(schemaName2))) {
            return true;
        }

        //switch off default names and then compare again
        if (schemaName1 == null) {
            if (accordingTo.supportsSchemas()) {
                schemaName1 = accordingTo.getDefaultSchemaName();
            } else if (accordingTo.supportsCatalogs()) {
                schemaName1 = accordingTo.getDefaultCatalogName();
            }
        }
        if (schemaName2 == null) {
            if (accordingTo.supportsSchemas()) {
                schemaName2 = accordingTo.getDefaultSchemaName();
            } else if (accordingTo.supportsCatalogs()) {
                schemaName2 = accordingTo.getDefaultCatalogName();
            }
        }
        if (StringUtils.trimToEmpty(schemaName1).equalsIgnoreCase(StringUtils.trimToEmpty(schemaName2))) {
            return true;
        }

        //check with schemaComparisons
        if ((chain.getSchemaComparisons() != null) && (chain.getSchemaComparisons().length > 0)) {
            for (CompareControl.SchemaComparison comparison : chain.getSchemaComparisons()) {
                String comparisonSchema1;
                String comparisonSchema2;
                if (accordingTo.supportsSchemas()) {
                    comparisonSchema1 = comparison.getComparisonSchema().getSchemaName();
                    comparisonSchema2 = comparison.getReferenceSchema().getSchemaName();
                } else if (accordingTo.supportsCatalogs()) {
                    comparisonSchema1 = comparison.getComparisonSchema().getCatalogName();
                    comparisonSchema2 = comparison.getReferenceSchema().getCatalogName();
                } else {
                    break;
                }

                String finalSchema1 = schemaName1;
                String finalSchema2 = schemaName2;

                if ((comparisonSchema1 != null) && comparisonSchema1.equalsIgnoreCase(schemaName1)) {
                    finalSchema1 = comparisonSchema2;
                } else if ((comparisonSchema2 != null) && comparisonSchema2.equalsIgnoreCase(schemaName1)) {
                    finalSchema1 = comparisonSchema1;
                }

                if (StringUtils.trimToEmpty(finalSchema1).equalsIgnoreCase(StringUtils.trimToEmpty(finalSchema2))) {
                    return true;
                }

                if ((comparisonSchema1 != null) && comparisonSchema1.equalsIgnoreCase(schemaName2)) {
                    finalSchema2 = comparisonSchema2;
                } else if ((comparisonSchema2 != null) && comparisonSchema2.equalsIgnoreCase(schemaName2)) {
                    finalSchema2 = comparisonSchema1;
                }

                if (StringUtils.trimToEmpty(finalSchema1).equalsIgnoreCase(StringUtils.trimToEmpty(finalSchema2))) {
                    return true;
                }
            }
        }

        schemaName1 = ((Schema) databaseObject1).toCatalogAndSchema().standardize(accordingTo).getSchemaName();
        schemaName2 = ((Schema) databaseObject2).toCatalogAndSchema().standardize(accordingTo).getSchemaName();

        return StringUtils.trimToEmpty(schemaName1).equalsIgnoreCase(StringUtils.trimToEmpty(schemaName2));
    }


    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude) {
        ObjectDifferences differences = new ObjectDifferences(compareControl);
        differences.compare("name", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Schema.class, accordingTo));

        return differences;
    }
}
