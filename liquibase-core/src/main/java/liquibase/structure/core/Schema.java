package liquibase.structure.core;

import liquibase.CatalogAndSchema;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.*;

public class Schema extends AbstractDatabaseObject {

    private Catalog catalog;
    private String name;
    private Map<Class<? extends DatabaseObject>, Set<DatabaseObject>> objects = new HashMap<Class<? extends DatabaseObject>, Set<DatabaseObject>>();

    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    public Schema() {
    }

    public Schema(String catalog, String schemaName) {
        catalog = StringUtils.trimToNull(catalog);
        schemaName = StringUtils.trimToNull(schemaName);

        this.name = schemaName;
        this.catalog = new Catalog(catalog);
    }
    
    public Schema(Catalog catalog, String name) {
        this(catalog.getName(), name);
    }

    public String getName() {
        return name;
    }
    
    public Schema getSchema() {
        return this;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schema schema = (Schema) o;

        if (catalog != null ? !catalog.equals(schema.catalog) : schema.catalog != null) return false;
        if (name != null ? !name.equals(schema.name) : schema.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = catalog != null ? catalog.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public String getCatalogName() {
        if (catalog == null) {
            return null;
        }
        return catalog.getName();
    }
    
    @Override
    public String toString() {
        return catalog.getName()+"."+name;
    }

    public CatalogAndSchema toCatalogAndSchema() {
        return new CatalogAndSchema(getCatalogName(), getName());
    }

    public <DatabaseObjectType extends DatabaseObject> List<DatabaseObjectType> getDatabaseObjects(Class<DatabaseObjectType> type) {
        Set<DatabaseObjectType> databaseObjects = (Set<DatabaseObjectType>) this.objects.get(type);
        if (databaseObjects == null) {
            return new ArrayList<DatabaseObjectType>();
        }
        return new ArrayList<DatabaseObjectType>(databaseObjects);
    }

    public void addDatabaseObject(DatabaseObject databaseObject) {
        if (databaseObject == null) {
            return;
        }
        Set<DatabaseObject> objects = this.objects.get(databaseObject.getClass());
        if (objects == null) {
            objects = new HashSet<DatabaseObject>();
            this.objects.put(databaseObject.getClass(), objects);
        }
        objects.add(databaseObject);

    }
}
