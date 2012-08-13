package liquibase.database.structure;

import liquibase.database.AbstractDatabase;
import liquibase.database.Database;
import liquibase.util.StringUtils;

public class Schema implements DatabaseObject {

    public static final String DEFAULT_NAME = "!DEFAULT_SCHEMA!";
    public static final Schema DEFAULT = new Schema(Catalog.DEFAULT, DEFAULT_NAME);

    private Catalog catalog;
    private String name;

    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    public Schema(String catalog, String schemaName) {
        if (StringUtils.trimToNull(schemaName) == null) {
            this.name = DEFAULT_NAME;
        } else {
            this.name = schemaName;
        }
        this.catalog = new Catalog(catalog);
    }
    
    public Schema(Catalog catalog, String name) {
        if (StringUtils.trimToNull(name) == null) {
            this.name = DEFAULT_NAME;
        } else {
            this.name = name;
        }
        this.catalog = catalog;
    }

    public String getName() {
        if (name.equals(DEFAULT_NAME)) {
            return null;
        }
        return name;
    }
    
    public String getName(Database database) {
        if (database == null) {
            return getName();
        }
        return database.correctSchemaName(getName());
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

        if (!catalog.equals(schema.catalog)) return false;
        if (!name.equals(schema.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = catalog.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public String getCatalogName() {
        return catalog.getName();
    }
    
    public String getCatalogName(Database database) {
        return catalog.getName(database);
    }

    public Schema clone(Database database) {
        return new Schema(catalog.getName(database), getName(database));
    }

    @Override
    public String toString() {
        return catalog.getName()+"."+name;
    }
}
