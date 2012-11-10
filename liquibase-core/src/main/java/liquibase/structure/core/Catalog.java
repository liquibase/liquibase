package liquibase.structure.core;

import liquibase.database.Database;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;

public class Catalog extends AbstractDatabaseObject {

    protected String name;

    public Catalog() {
    }

    public Catalog(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    public Schema getSchema() {
        return null;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Catalog catalog = (Catalog) o;

        if (name != null ? !name.equals(catalog.name) : catalog.name != null) return false;

        return true;
    }



    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public static class DatabaseSpecific extends Catalog {
        private Database database;

        public DatabaseSpecific(String name, Database database) {
            super(name);
            this.database = database;
            if (name == null) {
                this.name = null;
            }
        }

        public Database getDatabase() {
            return database;
        }

    }
}
