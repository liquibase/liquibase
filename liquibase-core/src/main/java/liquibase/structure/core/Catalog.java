package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;

import java.util.*;

public class Catalog extends AbstractDatabaseObject {

    public Catalog() {
        setAttribute("objects",  new HashMap<Class<? extends DatabaseObject>, Set<DatabaseObject>>());
    }

    public Catalog(String name) {
        this();
        setAttribute("name", name);
    }

    @Override
    public String toString() {
        String name = getName();
        if (name == null) {
            return "DEFAULT";
        }
        return name;
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    @Override
    public Schema getSchema() {
        return null;
    }

    @Override
    public String getName() {
        return getAttribute("name", String.class);
    }

    @Override
    public Catalog setName(String name) {
        setAttribute("name", name);
        return this;
    }

    public boolean isDefault() {
        return getAttribute("default", false) || (getName() == null);
    }

    public Catalog setDefault(Boolean isDefault) {
        setAttribute("default", isDefault);
        return this;
    }

    protected Map<Class<? extends DatabaseObject>, Set<DatabaseObject>> getObjects() {
        return getAttribute("objects", Map.class);
    }

    public <DatabaseObjectType extends DatabaseObject> List<DatabaseObjectType> getDatabaseObjects(Class<DatabaseObjectType> type) {
        Set<DatabaseObjectType> databaseObjects = (Set<DatabaseObjectType>) getObjects().get(type);
        if (databaseObjects == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(databaseObjects);
    }

    public void addDatabaseObject(DatabaseObject databaseObject) {
        if (databaseObject == null) {
            return;
        }
        Set<DatabaseObject> objects = this.getObjects().get(databaseObject.getClass());
        if (objects == null) {
            objects = new HashSet<>();
            this.getObjects().put(databaseObject.getClass(), objects);
        }
        objects.add(databaseObject);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;

        Catalog catalog = (Catalog) o;

        return (getName() != null) ? getName().equalsIgnoreCase(catalog.getName()) : (catalog.getName() == null);
    }



    @Override
    public int hashCode() {
        return (getName() != null) ? getName().hashCode() : 0;
    }

    @Override
    public Set<String> getSerializableFields() {
        Set<String> fields = super.getSerializableFields();
        fields.remove("objects");
        return fields;
    }
}
