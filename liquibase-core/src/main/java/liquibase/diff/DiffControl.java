package liquibase.diff;

import liquibase.database.structure.*;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

import java.util.*;

public class DiffControl {

    private Set<DiffStatusListener> statusListeners = new HashSet<DiffStatusListener>();

    private Schema[] referenceDatabaseSchemas;
    private Schema[] comparisonDatabaseSchemas;
    private List<Class<? extends DatabaseObject>> objectTypesToDiff= new ArrayList<Class<? extends DatabaseObject>>();
    private boolean diffData = false;

    private String dataDir = null;

    public DiffControl() {
        objectTypesToDiff.add(Table.class);
        objectTypesToDiff.add(View.class);
        objectTypesToDiff.add(Column.class);
        objectTypesToDiff.add(Index.class);
        objectTypesToDiff.add(ForeignKey.class);
        objectTypesToDiff.add(PrimaryKey.class);
        objectTypesToDiff.add(UniqueConstraint.class);
        objectTypesToDiff.add(Sequence.class);

        referenceDatabaseSchemas = new Schema[] {new Schema(new Catalog(null), null)};
        comparisonDatabaseSchemas = new Schema[] {new Schema(new Catalog(null), null)};
    }

    public DiffControl(Schema[] referenceDatabaseSchemas, Schema[] comparisonDatabaseSchemas, Class<? extends DatabaseObject>[] typesToDiff) {
        this.referenceDatabaseSchemas = referenceDatabaseSchemas;
        this.comparisonDatabaseSchemas = comparisonDatabaseSchemas;
        this.objectTypesToDiff = Arrays.asList(typesToDiff);
    }
    
    public DiffControl(Schema schema, Class<? extends DatabaseObject>... typesToDiff) {
        this(new Schema[] {schema}, null, typesToDiff);
    }

    public DiffControl(String referenceCatalogs, String referenceSchemas, String comparisonCatalogs, String comparisonSchemas, String diffTypes) {
        String[] splitReferenceSchemas = referenceSchemas.split(",");
        referenceDatabaseSchemas = new Schema[splitReferenceSchemas.length];
        for (int i=0; i<splitReferenceSchemas.length;i++){
            Schema schema = new Schema(new Catalog(null), splitReferenceSchemas[i]);
            referenceDatabaseSchemas[i] = schema;
        }

        String[] splitComparisonSchemas = comparisonSchemas.split(",");
        comparisonDatabaseSchemas = new Schema[splitComparisonSchemas.length];
        for (int i=0; i<splitComparisonSchemas.length;i++){
            Schema schema = new Schema(new Catalog(null), splitComparisonSchemas[i]);
            comparisonDatabaseSchemas[i] = schema;
        }

        if (StringUtils.trimToNull(diffTypes) != null) {
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

    public Schema[] getReferenceDatabaseSchemas() {
        return referenceDatabaseSchemas;
    }

    public Schema[] getComparisonDatabaseSchemas() {
        return comparisonDatabaseSchemas;
    }
    
    public Schema[] getSchemas(DatabaseRole databaseRole) {
        if (databaseRole.equals(DatabaseRole.COMPARISON)) {
            return comparisonDatabaseSchemas;
        } else if (databaseRole.equals(DatabaseRole.REFERENCE)) {
            return referenceDatabaseSchemas;
        } else {
            throw new UnexpectedLiquibaseException("Unknkown diff type: "+ databaseRole);
        }
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
    
}
