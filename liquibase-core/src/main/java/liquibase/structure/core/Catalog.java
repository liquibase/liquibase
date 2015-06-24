package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;

import java.util.*;

public class Catalog extends AbstractDatabaseObject {

    public Boolean isDefault;
    private Map<Class<? extends DatabaseObject>, Set<DatabaseObject>> objects;

    public Catalog() {
        this.objects = new HashMap<>();
    }

    public Catalog(ObjectName name) {
        super(name);
        this.objects = new HashMap<>();
    }

    public Catalog(String name) {
        this(new ObjectName(name));
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    @Override
    public Schema getSchema() {
        return null;
    }

    protected Map<Class<? extends DatabaseObject>, Set<DatabaseObject>> getObjects() {
        return objects;
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
        if (o == null || getClass() != o.getClass()) return false;

        Catalog catalog = (Catalog) o;

        if (getName() != null ? !getName().equals(catalog.getName()) : catalog.getName() != null) return false;

        return true;
    }



    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public Set<String> getSerializableFields() {
        Set<String> fields = super.getSerializableFields();
        fields.remove("objects");
        return fields;
    }
}
