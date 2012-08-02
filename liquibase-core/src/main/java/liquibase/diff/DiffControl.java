package liquibase.diff;

import liquibase.database.structure.*;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

import java.util.*;

public class DiffControl {

    private Set<DiffStatusListener> statusListeners = new HashSet<DiffStatusListener>();

    private SchemaComparison[] schemaComparisons;
    private List<Class<? extends DatabaseObject>> objectTypesToDiff = new ArrayList<Class<? extends DatabaseObject>>();
    private boolean diffData = false;

    private String dataDir = null;

    public DiffControl() {
        addDefaultTypes();

        schemaComparisons = new SchemaComparison[]{new SchemaComparison(new Schema(new Catalog(null), null), new Schema(new Catalog(null), null))};
    }

    private void addDefaultTypes() {
        objectTypesToDiff.add(Table.class);
        objectTypesToDiff.add(View.class);
        objectTypesToDiff.add(Column.class);
        objectTypesToDiff.add(Index.class);
        objectTypesToDiff.add(ForeignKey.class);
        objectTypesToDiff.add(PrimaryKey.class);
        objectTypesToDiff.add(UniqueConstraint.class);
        objectTypesToDiff.add(Sequence.class);
    }
    public DiffControl(SchemaComparison[] schemaComparison) {
        this(schemaComparison, (Class[]) null);
    }

    public DiffControl(SchemaComparison[] schemaComparison, Class<? extends DatabaseObject>[] typesToDiff) {
        if (typesToDiff == null) {
            addDefaultTypes();
        } else {
            this.objectTypesToDiff = Arrays.asList(typesToDiff);
        }
        this.schemaComparisons = schemaComparison;
    }

    public DiffControl(SchemaComparison[] schemaComparison, String typesToDiff) {
        this.schemaComparisons = schemaComparison;
        readDiffTypesString(typesToDiff);
    }

    public DiffControl(Schema schema, Class<? extends DatabaseObject>... typesToDiff) {
        this(new SchemaComparison[]{new SchemaComparison(schema, null)}, typesToDiff);
    }

    public DiffControl(Schema schema, String diffTypes) {
        this.schemaComparisons = new SchemaComparison[] {new SchemaComparison(schema, schema)};
        readDiffTypesString(diffTypes);
    }

    public DiffControl(String[] referenceVsComparisonSchemas, String diffTypes) {
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

            Schema referenceSchema = new Schema(new Catalog(referenceCatalogName), referenceSchemaName);
            Schema comparisonSchema = new Schema(new Catalog(comparisonCatalogName), comparisonSchemaName);
            this.schemaComparisons[i] = new SchemaComparison(referenceSchema, comparisonSchema);
        }

        readDiffTypesString(diffTypes);
    }

    private void readDiffTypesString(String diffTypes) {
        if (StringUtils.trimToNull(diffTypes) == null) {
            addDefaultTypes();
        } else {
            Set<String> types = new HashSet<String>(Arrays.asList(diffTypes.toLowerCase().split("\\s*,\\s*")));

            if (types.contains("tables")) {
                objectTypesToDiff.add(Table.class);
            }
            if (types.contains("views")) {
                objectTypesToDiff.add(View.class);
            }
            if (types.contains("columns")) {
                objectTypesToDiff.add(Column.class);
            }
            if (types.contains("indexes")) {
                objectTypesToDiff.add(Index.class);
            }
            if (types.contains("foreignkeys")) {
                objectTypesToDiff.add(ForeignKey.class);
            }
            if (types.contains("primarykeys")) {
                objectTypesToDiff.add(PrimaryKey.class);
            }
            if (types.contains("uniqueconstraints")) {
                objectTypesToDiff.add(UniqueConstraint.class);
            }
            if (types.contains("sequences")) {
                objectTypesToDiff.add(Sequence.class);
            }

            diffData = types.contains("data");
        }
    }

    public SchemaComparison[] getSchemaComparisons() {
        return schemaComparisons;
    }

    public Schema[] getSchemas(DatabaseRole databaseRole) {
        Schema[] schemas = new Schema[schemaComparisons.length];
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

    public boolean shouldDiff(Class<? extends DatabaseObject> type) {
        return objectTypesToDiff.contains(type);
    }

    public void setShouldDiff(Class<? extends DatabaseObject> type, boolean shouldDiff) {
        if (shouldDiff) {
            objectTypesToDiff.add(type);
        } else {
            objectTypesToDiff.remove(type);
        }
    }


    public boolean shouldDiffData() {
        return diffData;
    }

    public void setDiffData(boolean diffData) {
        this.diffData = diffData;
    }

    public Set<DiffStatusListener> getStatusListeners() {
        return statusListeners;
    }

    public void setStatusListeners(Set<DiffStatusListener> statusListeners) {
        this.statusListeners = statusListeners;
    }

    public void addStatusListener(DiffStatusListener statusListener) {
        this.statusListeners.add(statusListener);
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public List<Class<? extends DatabaseObject>> getTypesToCompare() {
        return objectTypesToDiff;
    }

    public static enum DatabaseRole {
        REFERENCE,
        COMPARISON
    }

    public static class SchemaComparison {
        private Schema comparisonSchema;
        private Schema referenceSchema;

        public SchemaComparison(Schema reference, Schema comparison) {
            this.referenceSchema = reference;
            this.comparisonSchema = comparison;
        }

        public Schema getComparisonSchema() {
            return comparisonSchema;
        }

        public Schema getReferenceSchema() {
            return referenceSchema;
        }
    }

}
