package liquibase.structure.core;

import liquibase.CatalogAndSchema;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.*;

public class Schema extends AbstractDatabaseObject {

    public Schema() {
        setAttribute("objects",  new HashMap<Class<? extends DatabaseObject>, Set<DatabaseObject>>());
    }

    public Schema(String catalog, String schemaName) {
        this(new Catalog(catalog), schemaName);
    }

    public Schema(Catalog catalog, String schemaName) {
        schemaName = StringUtils.trimToNull(schemaName);

        setAttribute("name", schemaName);
        setAttribute("catalog", catalog);
        setAttribute("objects", new HashMap<Class<? extends DatabaseObject>, Set<DatabaseObject>>());
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    @Override
    public String getName() {
        return getAttribute("name", String.class);
    }

    @Override
    public Schema setName(String name) {
        setAttribute("name", name);
        return this;
    }

    public boolean isDefault() {
        return getAttribute("default", false) || (getName() == null);
    }

    public Schema setDefault(Boolean isDefault) {
        setAttribute("default", isDefault);
        return this;
    }


    @Override
    public Schema getSchema() {
        return this;
    }

    public Catalog getCatalog() {
        return getAttribute("catalog", Catalog.class);
    }

    @Override
    public boolean equals(Object o) {
        // object identity
        if (this == o) {
            return true;
        }

        // other object null or of different class
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        Schema otherSchema = (Schema) o;

        // catalog or name different?
        return (
            (Objects.equals(getCatalog(), otherSchema.getCatalog())) &&
                (StringUtils.equalsIgnoreCaseAndEmpty(getName(), otherSchema.getName())));
    }

    @Override
    public int hashCode() {
        int result = (getCatalog() != null) ? getCatalog().hashCode() : 0;
        result = (31 * result) + ((getName() != null) ? getName().hashCode() : 0);
        return result;
    }

    public String getCatalogName() {
        if (getCatalog() == null) {
            return null;
        }
        return getCatalog().getName();
    }
    
    @Override
    public String toString() {
        String catalogName = getCatalogName();

        String schemaName = getName();
        if (schemaName == null) {
            schemaName = "DEFAULT";
        }

        if ((catalogName == null) || catalogName.equals(schemaName)) {
            return schemaName;
        } else {
            return catalogName +"."+ schemaName;
        }
    }

    public CatalogAndSchema toCatalogAndSchema() {
        String catalogName;
        if ((getCatalog() != null) && getCatalog().isDefault()) {
            catalogName = null;
        } else {
            catalogName = getCatalogName();
        }

        String schemaName;
        if (isDefault()) {
            schemaName = null;
        } else {
            schemaName = getName();
        }
        return new CatalogAndSchema(catalogName, schemaName);
    }

    protected Map<Class<? extends DatabaseObject>, Set<DatabaseObject>> getObjects() {
        return getAttribute("objects", Map.class);
    }

    public <T extends DatabaseObject> List<T> getDatabaseObjects(Class<T> type) {
        Set<T> databaseObjects =
            (Set<T>) getObjects().get(type);
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
    public Set<String> getSerializableFields() {
        Set<String> fields = super.getSerializableFields();
        fields.remove("objects");
        return fields;
    }
}
