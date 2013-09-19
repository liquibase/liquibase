package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DiffOutputControl {
    private boolean includeSchema;
    private boolean includeCatalog;
    private boolean includeTablespace;

    private String dataDir = null;
    private Map<String, Set<DatabaseObject>> alreadyHandledMissingByHash = new HashMap<String, Set<DatabaseObject>>();
    private Map<String, Set<DatabaseObject>> alreadyHandledUnexpectedByHash = new HashMap<String, Set<DatabaseObject>>();
    private Map<String, Set<DatabaseObject>> alreadyHandledChangedByHash = new HashMap<String, Set<DatabaseObject>>();
    private DatabaseForHash databaseForHash = new DatabaseForHash();

    public DiffOutputControl() {
        includeSchema = true;
        includeCatalog = true;
        includeTablespace = true;
    }

    public DiffOutputControl(boolean includeCatalog, boolean includeSchema, boolean includeTablespace) {
        this.includeSchema = includeSchema;
        this.includeCatalog = includeCatalog;
        this.includeTablespace = includeTablespace;
    }

    public boolean isIncludeSchema() {
        return includeSchema;
    }

    public DiffOutputControl setIncludeSchema(boolean includeSchema) {
        this.includeSchema = includeSchema;
        return this;
    }

    public boolean isIncludeCatalog() {
        return includeCatalog;
    }

    public DiffOutputControl setIncludeCatalog(boolean includeCatalog) {
        this.includeCatalog = includeCatalog;
        return this;
    }

    public boolean isIncludeTablespace() {
        return includeTablespace;
    }

    public DiffOutputControl setIncludeTablespace(boolean includeTablespace) {
        this.includeTablespace = includeTablespace;
        return this;
    }

    public String getDataDir() {
        return dataDir;
    }

    public DiffOutputControl setDataDir(String dataDir) {
        this.dataDir = dataDir;
        return this;
    }

    public void setAlreadyHandledMissing(DatabaseObject missingObject) {
        if (missingObject == null) {
            return;
        }
        String hash = DatabaseObjectComparatorFactory.getInstance().hash(missingObject, databaseForHash);
        if (!alreadyHandledMissingByHash.containsKey(hash)) {
            alreadyHandledMissingByHash.put(hash, new HashSet<DatabaseObject>());
        }

        this.alreadyHandledMissingByHash.get(hash).add(missingObject);
    }

    public boolean alreadyHandledMissing(DatabaseObject missingObject, Database accordingTo) {
        String hash = DatabaseObjectComparatorFactory.getInstance().hash(missingObject, databaseForHash);

        if (!alreadyHandledMissingByHash.containsKey(hash)) {
            return false;
        }

        for (DatabaseObject object : this.alreadyHandledMissingByHash.get(hash)) {
            if (DatabaseObjectComparatorFactory.getInstance().isSameObject(object, missingObject, accordingTo)) {
                return true;
            }
        }
        return false;
    }

    public void setAlreadyHandledUnexpected(DatabaseObject unexpectedObject) {
        if (unexpectedObject == null) {
            return;
        }

        String hash = DatabaseObjectComparatorFactory.getInstance().hash(unexpectedObject, databaseForHash);
        if (!alreadyHandledUnexpectedByHash.containsKey(hash)) {
            alreadyHandledUnexpectedByHash.put(hash, new HashSet<DatabaseObject>());
        }

        this.alreadyHandledUnexpectedByHash.get(hash).add(unexpectedObject);
    }

    public boolean alreadyHandledUnexpected(DatabaseObject unexpectedObject, Database accordingTo) {
        String hash = DatabaseObjectComparatorFactory.getInstance().hash(unexpectedObject, databaseForHash);

        if (!alreadyHandledUnexpectedByHash.containsKey(hash)) {
            return false;
        }

        for (DatabaseObject object : this.alreadyHandledUnexpectedByHash.get(hash)) {
            if (DatabaseObjectComparatorFactory.getInstance().isSameObject(object, unexpectedObject, accordingTo)) {
                return true;
            }
        }
        return false;
    }

    public void setAlreadyHandledChanged(DatabaseObject changedObject) {
        if (changedObject == null) {
            return;
        }

        String hash = DatabaseObjectComparatorFactory.getInstance().hash(changedObject, databaseForHash);
        if (!alreadyHandledChangedByHash.containsKey(hash)) {
            alreadyHandledChangedByHash.put(hash, new HashSet<DatabaseObject>());
        }

        this.alreadyHandledChangedByHash.get(hash).add(changedObject);
    }

    public boolean alreadyHandledChanged(DatabaseObject changedObject, Database accordingTo) {
        String hash = DatabaseObjectComparatorFactory.getInstance().hash(changedObject, databaseForHash);

        if (!alreadyHandledChangedByHash.containsKey(hash)) {
            return false;
        }

        for (DatabaseObject object : this.alreadyHandledChangedByHash.get(hash)) {
            if (DatabaseObjectComparatorFactory.getInstance().isSameObject(object, changedObject, accordingTo)) {
                return true;
            }
        }
        return false;
    }

    private static class DatabaseForHash extends H2Database {
        @Override
        public boolean isCaseSensitive() {
            return true;
        }
    }

}
