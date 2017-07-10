package liquibase.diff.output;

import liquibase.CatalogAndSchema;
import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.database.Database;
import liquibase.database.InternalDatabase;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.H2Database;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.changelog.ChangeGeneratorFactory;
import liquibase.diff.output.changelog.core.MissingDataExternalFileChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.core.Schema;

import java.util.HashSet;
import java.util.Set;

public class DiffOutputControl {

    private Set<CatalogAndSchema> includeSchemas = new HashSet<>();

    private boolean includeSchema;
    private boolean includeCatalog;
    private boolean includeTablespace;

    private CompareControl.SchemaComparison[] schemaComparisons;

    private DatabaseObjectCollection alreadyHandledMissing= new DatabaseObjectCollection(new DatabaseForHash());
    private DatabaseObjectCollection alreadyHandledUnexpected = new DatabaseObjectCollection(new DatabaseForHash());
    private DatabaseObjectCollection alreadyHandledChanged = new DatabaseObjectCollection(new DatabaseForHash());
    private ObjectQuotingStrategy objectQuotingStrategy;

    private ContextExpression context;
    private Labels labels;

    private ObjectChangeFilter objectChangeFilter;

    public DiffOutputControl() {
        includeSchema = true;
        includeCatalog = true;
        includeTablespace = true;
    }

    public DiffOutputControl(boolean includeCatalog, boolean includeSchema, boolean includeTablespace, CompareControl.SchemaComparison[] schemaComparisons) {
        this.includeSchema = includeSchema;
        this.includeCatalog = includeCatalog;
        this.includeTablespace = includeTablespace;
        this.schemaComparisons = schemaComparisons;
    }

    public CompareControl.SchemaComparison[] getSchemaComparisons() {
        return schemaComparisons;
    }

    public boolean getIncludeSchema() {
        return includeSchema;
    }

    public DiffOutputControl setIncludeSchema(boolean includeSchema) {
        this.includeSchema = includeSchema;
        return this;
    }

    public boolean getIncludeCatalog() {
        return includeCatalog;
    }

    public DiffOutputControl setIncludeCatalog(boolean includeCatalog) {
        this.includeCatalog = includeCatalog;
        return this;
    }

    public boolean getIncludeTablespace() {
        return includeTablespace;
    }

    public DiffOutputControl setIncludeTablespace(boolean includeTablespace) {
        this.includeTablespace = includeTablespace;
        return this;
    }

    public DiffOutputControl setDataDir(String dataDir) {

        if (dataDir != null) {
            ChangeGeneratorFactory.getInstance().register(new MissingDataExternalFileChangeGenerator(dataDir));
        }
        return this;
    }

    public void setAlreadyHandledMissing(DatabaseObject missingObject) {
        this.alreadyHandledMissing.add(missingObject);
    }

    public boolean alreadyHandledMissing(DatabaseObject missingObject, Database accordingTo) {
        return alreadyHandledMissing.contains(missingObject, schemaComparisons);
    }

    public void setAlreadyHandledUnexpected(DatabaseObject unexpectedObject) {
        this.alreadyHandledUnexpected.add(unexpectedObject);
    }

    public boolean alreadyHandledUnexpected(DatabaseObject unexpectedObject, Database accordingTo) {
        return alreadyHandledUnexpected.contains(unexpectedObject, schemaComparisons);
    }

    public void setAlreadyHandledChanged(DatabaseObject changedObject) {
        this.alreadyHandledChanged.add(changedObject);
    }

    public boolean alreadyHandledChanged(DatabaseObject changedObject, Database accordingTo) {
        return alreadyHandledChanged.contains(changedObject, schemaComparisons);
    }

    public DiffOutputControl addIncludedSchema(Schema schema) {
        this.includeSchemas.add(schema.toCatalogAndSchema());
        return this;
    }

    public DiffOutputControl addIncludedSchema(CatalogAndSchema schema) {
        this.includeSchemas.add(schema);
        return this;
    }

    public boolean shouldOutput(DatabaseObject object, Database accordingTo) {
        if (!includeSchemas.isEmpty()) {
            Schema schema = object.getSchema();
            if (schema == null) {
                return true;
            }
            CatalogAndSchema objectCatalogAndSchema = schema.toCatalogAndSchema().standardize(accordingTo);
            for (CatalogAndSchema catalogAndSchema : includeSchemas) {
                catalogAndSchema = schema.toCatalogAndSchema().standardize(accordingTo);
                if (objectCatalogAndSchema.equals(catalogAndSchema, accordingTo)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public ObjectChangeFilter getObjectChangeFilter() {
        return objectChangeFilter;
    }

    public DiffOutputControl setObjectChangeFilter(ObjectChangeFilter objectChangeFilter) {
        this.objectChangeFilter = objectChangeFilter;
        return this;
    }

    public ContextExpression getContext() {
        return context;
    }

    public DiffOutputControl setContext(ContextExpression contextExpression) {
        this.context = contextExpression;
        return this;
    }

    public Labels getLabels() {
        return labels;
    }

    public DiffOutputControl setLabels(Labels labels) {
        this.labels = labels;
        return this;
    }

    public ObjectQuotingStrategy getObjectQuotingStrategy() {
        return objectQuotingStrategy;
    }

    public DiffOutputControl setObjectQuotingStrategy(ObjectQuotingStrategy objectQuotingStrategy) {
        this.objectQuotingStrategy = objectQuotingStrategy;
        return this;
    }

    private static class DatabaseForHash extends H2Database implements InternalDatabase {
        @Override
        public boolean isCaseSensitive() {
            return true;
        }
    }

}
