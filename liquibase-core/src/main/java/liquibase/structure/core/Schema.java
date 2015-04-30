package liquibase.structure.core;

import liquibase.CatalogAndSchema;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.util.StringUtils;

import java.util.*;

public class Schema extends AbstractDatabaseObject {

    public static enum Attr {
        objects
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    public Schema() {
        set(Attr.objects, new HashMap<Class<? extends DatabaseObject>, Set<DatabaseObject>>());
    }

    public Schema(ObjectName name) {
        super(name);
        set(Attr.objects, new HashMap<Class<? extends DatabaseObject>, Set<DatabaseObject>>());
    }

    public Schema(String catalog, String schemaName) {
        this(new ObjectName(catalog, schemaName));
    }
    
    public Schema(Catalog catalog, String schemaName) {
        this(catalog.getName().getName(), schemaName);
    }

    public boolean isDefault() {
        return get("default", false);
    }

    public Schema setDefault(Boolean isDefault) {
        set("default", isDefault);
        return this;
    }


    @Override
    public Schema getSchema() {
        return this;
    }

    public Catalog getCatalog() {
        return new Catalog(getName().getContainer().getName());
    }

    public String getCatalogName() {
        if (getCatalog() == null) {
            return null;
        }
        return getCatalog().getSimpleName();
    }
    
    @Override
    public String toString() {
        return getName().toString();
    }

    public CatalogAndSchema toCatalogAndSchema() {
        String catalogName;
        if (getCatalog() != null && getCatalog().isDefault()) {
            catalogName = null;
        } else {
            catalogName = getCatalogName();
        }

        String schemaName;
        if (isDefault()) {
            schemaName = null;
        } else {
            schemaName = getSimpleName();
        }
        return new CatalogAndSchema(catalogName, schemaName);
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
    public Set<String> getSerializableFields() {
        Set<String> fields = super.getSerializableFields();
        fields.remove("objects");
        return fields;
    }
}
