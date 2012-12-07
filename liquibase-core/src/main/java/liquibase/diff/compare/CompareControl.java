package liquibase.diff.compare;

import liquibase.CatalogAndSchema;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;

import java.util.HashSet;
import java.util.Set;

public class CompareControl {

    private static Set<Class<? extends DatabaseObject>> defaultTypes;

    private CompareControl.SchemaComparison[] schemaComparisons;
    private Set<Class<? extends DatabaseObject>> compareTypes = new HashSet<Class<? extends DatabaseObject>>();

    public CompareControl() {
        this(null);
    }

    public CompareControl(Set<Class<? extends DatabaseObject>> compareTypes) {
        schemaComparisons = new SchemaComparison[]{new SchemaComparison(new CatalogAndSchema(null, null), new CatalogAndSchema(null, null))};
        initCompareTypes(compareTypes);
    }

    public CompareControl(SchemaComparison[] schemaComparison, Set<Class<? extends DatabaseObject>> compareTypes) {
        this.schemaComparisons = schemaComparison;
        initCompareTypes(compareTypes);
    }

    public CompareControl(String[] referenceVsComparisonSchemas, Set<Class<? extends DatabaseObject>> compareTypes) {
        String[] splitReferenceSchemas = referenceVsComparisonSchemas[0].split(",");
        String[] splitComparisonSchemas = referenceVsComparisonSchemas[1].split(",");
        this.schemaComparisons = new SchemaComparison[splitReferenceSchemas.length];
        for (int i = 0; i < splitReferenceSchemas.length; i++) {
            String referenceCatalogName = null;
            String referenceSchemaName = splitReferenceSchemas[i];
            if (referenceSchemaName.contains(".")) {
                referenceCatalogName = referenceSchemaName.split(".", 2)[0];
                referenceSchemaName = referenceSchemaName.split(".", 2)[1];
            }

            String comparisonCatalogName = null;
            String comparisonSchemaName = splitComparisonSchemas[i];
            if (comparisonSchemaName.contains(".")) {
                comparisonCatalogName = comparisonSchemaName.split(".", 2)[0];
                comparisonSchemaName = comparisonSchemaName.split(".", 2)[1];
            }

            CatalogAndSchema referenceSchema = new CatalogAndSchema(referenceCatalogName, referenceSchemaName);
            CatalogAndSchema comparisonSchema = new CatalogAndSchema(comparisonCatalogName, comparisonSchemaName);
            this.schemaComparisons[i] = new SchemaComparison(referenceSchema, comparisonSchema);

            initCompareTypes(compareTypes);
        }
    }

    private void initCompareTypes(Set<Class<? extends DatabaseObject>> compareTypes) {
        if (compareTypes == null || compareTypes.size() == 0) {
            compareTypes = getDefaultTypes();
        }
        this.compareTypes = compareTypes;
    }

    private Set<Class<? extends DatabaseObject>> getDefaultTypes() {
        if (defaultTypes == null) {
            Set<Class<? extends DatabaseObject>> set = new HashSet<Class<? extends DatabaseObject>>();

            Class<? extends DatabaseObject>[] classes = ServiceLocator.getInstance().findClasses(DatabaseObject.class);
            for (Class<? extends DatabaseObject> clazz : classes) {
                try {
                    if (clazz.newInstance().snapshotByDefault()) {
                        set.add(clazz);
                    }
                } catch (Exception e) {
                    LogFactory.getLogger().info("Cannot construct "+clazz.getName()+" to determine if it should be included in the snapshot by default");
                }
            }

            defaultTypes = set;
        }
        return defaultTypes;
    }
    public Set<Class<? extends DatabaseObject>> getComparedTypes() {
        return compareTypes;
    }



    public SchemaComparison[] getSchemaComparisons() {
        return schemaComparisons;
    }

    public CatalogAndSchema[] getSchemas(DatabaseRole databaseRole) {
        CatalogAndSchema[] schemas = new CatalogAndSchema[schemaComparisons.length];
        for (int i=0; i<schemaComparisons.length; i++) {
            if (databaseRole.equals(DatabaseRole.COMPARISON)) {
                schemas[i] = schemaComparisons[i].getComparisonSchema();
            } else if (databaseRole.equals(DatabaseRole.REFERENCE)) {
                schemas[i] = schemaComparisons[i].getReferenceSchema();
            } else {
                throw new UnexpectedLiquibaseException("Unknkown diff type: " + databaseRole);
            }
        }
        return schemas;
    }

    public static enum DatabaseRole {
        REFERENCE,
        COMPARISON
    }

    public static class SchemaComparison {
        private CatalogAndSchema comparisonSchema;
        private CatalogAndSchema referenceSchema;

        public SchemaComparison(CatalogAndSchema reference, CatalogAndSchema comparison) {
            this.referenceSchema = reference;
            this.comparisonSchema = comparison;
        }

        public CatalogAndSchema getComparisonSchema() {
            return comparisonSchema;
        }

        public CatalogAndSchema getReferenceSchema() {
            return referenceSchema;
        }
    }

}
