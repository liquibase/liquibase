package liquibase.structure.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectImpl;
import liquibase.util.StringUtils;

public class Schema extends DatabaseObjectImpl {

    protected Catalog catalog;
    protected String name;

    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    public Schema(String catalog, String schemaName) {
        setPartial(false);

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
        return catalog.getName();
    }
    
    @Override
    public String toString() {
        return catalog.getName()+"."+name;
    }

    public CatalogAndSchema toCatalogAndSchema() {
        return new CatalogAndSchema(getCatalogName(), getName());
    }


    public static class DatabaseSpecific extends Schema {
        private Database database;

        public DatabaseSpecific(String catalog, String schemaName, Database database) {
            super(catalog, schemaName);
            this.database = database;
            if (catalog == null) {
                this.catalog = new Catalog.DatabaseSpecific(null, database);
            }
            if (schemaName == null) {
                this.name = null;
            }
        }

        public DatabaseSpecific(Catalog catalog, String name) {
            super(catalog, name);
            if (catalog == null) {
                this.catalog = new Catalog.DatabaseSpecific(null, database);
            }
            if (name == null) {
                this.name = null;
            }
        }

        public Database getDatabase() {
            return database;
        }
    }
}
