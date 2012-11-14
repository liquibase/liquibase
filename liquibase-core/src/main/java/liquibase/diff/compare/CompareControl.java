package liquibase.diff.compare;

import liquibase.CatalogAndSchema;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.DatabaseObject;

import java.util.HashSet;
import java.util.Set;

public class CompareControl {

    private CompareControl.SchemaComparison[] schemaComparisons;
    private Set<Class<? extends DatabaseObject>> compareTypes = new HashSet<Class<? extends DatabaseObject>>();

    public CompareControl() {
        schemaComparisons = new SchemaComparison[]{new SchemaComparison(new CatalogAndSchema(null, null), new CatalogAndSchema(null, null))};
    }

    public CompareControl(SchemaComparison[] schemaComparison) {
        this.schemaComparisons = schemaComparison;
    }

    public CompareControl(String[] referenceVsComparisonSchemas) {
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
        }
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
