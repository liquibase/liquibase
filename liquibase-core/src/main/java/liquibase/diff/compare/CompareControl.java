package liquibase.diff.compare;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.DatabaseObjectFactory;
import liquibase.util.StringUtil;

import java.util.*;

public class CompareControl {

    public static CompareControl STANDARD = new CompareControl();
    private CompareControl.SchemaComparison[] schemaComparisons;
    private Set<Class<? extends DatabaseObject>> compareTypes = new HashSet<>();
    private Map<Class<? extends DatabaseObject>, Set<String>> suppressedFields = new HashMap<>();


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
        if ((schemaComparison != null) && (schemaComparison.length > 0)) {
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

    public static ComputedSchemas computeSchemas(String schemaNames, String referenceSchemaNames, String
        outputSchemaNames, String defaultCatalogName, String defaultSchemaName, String referenceDefaultCatalogName,
                                                 String referenceDefaultSchemaName, Database database) {

        //Make sure either both schemaNames and referenceSchemaNames are set or both are null. If only one is set,
        // make them equal
        if ((schemaNames == null) && (referenceSchemaNames == null)) {
            //they will be set to the defaults
        } else if ((schemaNames == null) && (referenceSchemaNames != null)) {
            schemaNames = referenceSchemaNames;
        } else if ((schemaNames != null) && (referenceSchemaNames == null)) {
            referenceSchemaNames = schemaNames;
        }

        if ((schemaNames == null) && (outputSchemaNames != null)) {
            if (defaultSchemaName == null) {
                schemaNames = database.getDefaultSchemaName();
            } else {
                schemaNames = defaultSchemaName;
            }
            referenceSchemaNames = schemaNames;
        }

        ComputedSchemas returnObj = new ComputedSchemas();
        if (referenceSchemaNames == null) {
            returnObj.finalSchemaComparisons = new CompareControl.SchemaComparison[]{new CompareControl
                .SchemaComparison(
                new CatalogAndSchema(referenceDefaultCatalogName, referenceDefaultSchemaName),
                new CatalogAndSchema(defaultCatalogName, defaultSchemaName)
            )};
            returnObj.finalTargetSchemas = new CatalogAndSchema[]{new CatalogAndSchema(defaultCatalogName,
                defaultSchemaName)};
        } else {
            List<SchemaComparison> schemaComparisons = new ArrayList<>();
            List<CatalogAndSchema> referenceSchemas = new ArrayList<>();
            List<CatalogAndSchema> targetSchemas = new ArrayList<>();

            List<String> splitReferenceSchemaNames = StringUtil.splitAndTrim(referenceSchemaNames, ",");
            List<String> splitSchemaNames = StringUtil.splitAndTrim(schemaNames, ",");
            List<String> splitOutputSchemaNames = StringUtil.splitAndTrim(StringUtil.trimToNull(outputSchemaNames),
                ",");

            if (splitReferenceSchemaNames.size() != splitSchemaNames.size()) {
                throw new UnexpectedLiquibaseException("You must specify the same number of schemas in --schemas and " +
                    "--referenceSchemas");
            }
            if ((splitOutputSchemaNames != null) && (splitOutputSchemaNames.size() != splitSchemaNames.size())) {
                throw new UnexpectedLiquibaseException("You must specify the same number of schemas in --schemas and " +
                    "--outputSchemasAs");
            }

            for (int i = 0; i < splitReferenceSchemaNames.size(); i++) {
                String referenceSchema = splitReferenceSchemaNames.get(i);
                String targetSchema = splitSchemaNames.get(i);
                String outputSchema = null;
                if (splitOutputSchemaNames != null) {
                    outputSchema = splitOutputSchemaNames.get(i);
                }

                CatalogAndSchema correctedTargetSchema = new CatalogAndSchema(null, targetSchema).customize(database);
                CatalogAndSchema correctedReferenceSchema = new CatalogAndSchema(null, referenceSchema).customize
                    (database);
                SchemaComparison comparison = new SchemaComparison(correctedReferenceSchema, correctedTargetSchema);
                comparison.setOutputSchemaAs(outputSchema);
                schemaComparisons.add(comparison);
                referenceSchemas.add(correctedReferenceSchema);
                targetSchemas.add(correctedTargetSchema);
            }
            returnObj.finalSchemaComparisons = schemaComparisons.toArray(new CompareControl
                .SchemaComparison[schemaComparisons.size()]);
            returnObj.finalTargetSchemas = targetSchemas.toArray(new CatalogAndSchema[targetSchemas.size()]);
        }

        return returnObj;
    }

    protected void setTypes(Set<Class<? extends DatabaseObject>> types) {
        if ((types == null) || types.isEmpty()) {
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

    public enum DatabaseRole {
        REFERENCE,
        COMPARISON
    }

    public static class SchemaComparison {
        private CatalogAndSchema comparisonSchema;
        private CatalogAndSchema referenceSchema;
        private String outputSchemaAs;

        public SchemaComparison(CatalogAndSchema reference, CatalogAndSchema comparison) {
            this.referenceSchema = reference;
            this.comparisonSchema = comparison;
        }

        public static String convertSchema(String schemaName, SchemaComparison[] schemaComparisons) {
            if ((schemaComparisons == null) || (schemaComparisons.length == 0) || (schemaName == null)) {
                return schemaName;
            }

            String convertedSchemaName = null;
            for (CompareControl.SchemaComparison comparison : schemaComparisons) {
                if (schemaName.equals(comparison.getComparisonSchema().getSchemaName())) {
                    convertedSchemaName = comparison.getReferenceSchema().getSchemaName();
                } else if (schemaName.equals(comparison.getComparisonSchema().getCatalogName())) {
                    convertedSchemaName = comparison.getReferenceSchema().getCatalogName();

                } else if (schemaName.equals(comparison.getReferenceSchema().getSchemaName())) {
                    convertedSchemaName = comparison.getComparisonSchema().getSchemaName();
                } else if (schemaName.equals(comparison.getReferenceSchema().getCatalogName())) {
                    convertedSchemaName = comparison.getComparisonSchema().getCatalogName();
                }
            }

            if (convertedSchemaName == null) {
                return schemaName;
            } else {
                return convertedSchemaName;
            }
        }

        public CatalogAndSchema getComparisonSchema() {
            return comparisonSchema;
        }

        public CatalogAndSchema getReferenceSchema() {
            return referenceSchema;
        }

        public String getOutputSchemaAs() {
            return outputSchemaAs;
        }

        public void setOutputSchemaAs(String outputSchemaAs) {
            this.outputSchemaAs = outputSchemaAs;
        }
    }

    public static class ComputedSchemas {
        public CompareControl.SchemaComparison[] finalSchemaComparisons;
        public CatalogAndSchema[] finalTargetSchemas;
    }
}
