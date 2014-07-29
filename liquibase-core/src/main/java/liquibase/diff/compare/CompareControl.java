package liquibase.diff.compare;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.servicelocator.ServiceLocator;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.DatabaseObjectFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompareControl {

    private CompareControl.SchemaComparison[] schemaComparisons;
    private Set<Class<? extends DatabaseObject>> compareTypes = new HashSet<Class<? extends DatabaseObject>>();
    private Map<Class<? extends DatabaseObject>, Set<String>> suppressedFields = new HashMap<Class<? extends DatabaseObject>, Set<String>>();

    public static CompareControl STANDARD = new CompareControl();


    public CompareControl() {
        this(null);
    }

    public CompareControl(Set<Class<? extends DatabaseObject>> compareTypes) {
        schemaComparisons = new SchemaComparison[]{new SchemaComparison(new CatalogAndSchema(null, null), new CatalogAndSchema(null, null))};
        setTypes(compareTypes);
    }

    public CompareControl(SchemaComparison[] schemaComparison, Set<Class<? extends DatabaseObject>> compareTypes) {
        this.schemaComparisons = schemaComparison;
        setTypes(compareTypes);
    }

    public CompareControl(SchemaComparison[] schemaComparison, String compareTypes) {
        if (schemaComparison != null && schemaComparison.length > 0) {
            this.schemaComparisons = schemaComparison;
        } else {
            this.schemaComparisons = new SchemaComparison[]{new SchemaComparison(new CatalogAndSchema(null, null), new CatalogAndSchema(null, null))};
        }
        setTypes(DatabaseObjectFactory.getInstance().parseTypes(compareTypes));
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

            setTypes(compareTypes);
        }
    }

    protected void setTypes(Set<Class<? extends DatabaseObject>> types) {
        if (types == null || types.size() == 0) {
            types = DatabaseObjectFactory.getInstance().getStandardTypes();
        }
        this.compareTypes = types;
    }

    public Set<Class<? extends DatabaseObject>> getComparedTypes() {
        return compareTypes;
    }


    public CompareControl addSuppressedField(Class<? extends DatabaseObject> type, String field) {
        if (!suppressedFields.containsKey(type)) {
            suppressedFields.put(type, new HashSet<String>());
        }
        suppressedFields.get(type).add(field);

        return this;
    }

    public boolean isSuppressedField(Class<? extends DatabaseObject> type, String field) {
        if (!suppressedFields.containsKey(type)) {
            return false;
        }
        return suppressedFields.get(type).contains(field);
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
