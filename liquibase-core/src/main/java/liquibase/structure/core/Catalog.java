package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;

import java.util.*;

public class Catalog extends AbstractDatabaseObject {

    public Catalog() {
        set("objects", new HashMap<Class<? extends DatabaseObject>, Set<DatabaseObject>>());
    }

    public Catalog(String name) {
        this();
        set("name", name);
    }

    @Override
    public String toString() {
        ObjectName name = getName();
        if (name == null) {
            return "DEFAULT";
        }
        return name.getName();
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    @Override
    public Schema getSchema() {
        return null;
    }

    public boolean isDefault() {
        return get("default", false);
    }

    public Catalog setDefault(Boolean isDefault) {
        set("default", isDefault);
        return this;
    }

    protected Map<Class<? extends DatabaseObject>, Set<DatabaseObject>> getObjects() {
        return get("objects", Map.class);
    }

    public <DatabaseObjectType extends DatabaseObject> List<DatabaseObjectType> getDatabaseObjects(Class<DatabaseObjectType> type) {
        Set<DatabaseObjectType> databaseObjects = (Set<DatabaseObjectType>) getObjects().get(type);
        if (databaseObjects == null) {
            return new ArrayList<DatabaseObjectType>();
        }
        return new ArrayList<DatabaseObjectType>(databaseObjects);
    }

    public void addDatabaseObject(DatabaseObject databaseObject) {
        if (databaseObject == null) {
            return;
        }
        Set<DatabaseObject> objects = this.getObjects().get(databaseObject.getClass());
        if (objects == null) {
            objects = new HashSet<DatabaseObject>();
            this.getObjects().put(databaseObject.getClass(), objects);
        }
        objects.add(databaseObject);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Catalog catalog = (Catalog) o;

        if (getName() != null ? !getName().equals(catalog.getName()) : catalog.getName() != null) return false;

        return true;
    }



    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }


}
