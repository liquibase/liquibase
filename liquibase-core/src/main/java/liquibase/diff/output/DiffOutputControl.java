package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;

import java.util.HashSet;
import java.util.Set;

public class DiffOutputControl {
    private boolean includeSchema;
    private boolean includeCatalog;
    private boolean includeTablespace;

    private String dataDir = null;
    private Set<DatabaseObject> alreadyHandledMissing = new HashSet<DatabaseObject>();
    private Set<DatabaseObject> alreadyHandledUnexpected = new HashSet<DatabaseObject>();
    private Set<DatabaseObject> alreadyHandledChanged = new HashSet<DatabaseObject>();

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
        this.alreadyHandledMissing.add(missingObject);
    }

    public boolean alreadyHandledMissing(DatabaseObject missingObject, Database accordingTo) {
        for (DatabaseObject object : this.alreadyHandledMissing) {
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
        this.alreadyHandledUnexpected.add(unexpectedObject);
    }

    public boolean alreadyHandledUnexpected(DatabaseObject unexpectedObject, Database accordingTo) {
        for (DatabaseObject object : this.alreadyHandledUnexpected) {
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

        this.alreadyHandledChanged.add(changedObject);
    }

    public boolean alreadyHandledChanged(DatabaseObject changedObject, Database accordingTo) {
        for (DatabaseObject object : this.alreadyHandledChanged) {
            if (DatabaseObjectComparatorFactory.getInstance().isSameObject(object, changedObject, accordingTo)) {
                return true;
            }
        }
        return false;
    }
}
