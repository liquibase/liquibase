package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;

public class Catalog extends AbstractDatabaseObject {

    public Catalog() {
    }

    public Catalog(String name) {
        setAttribute("name", name);
    }

    @Override
    public String toString() {
        return getName();
    }

    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    public Schema getSchema() {
        return null;
    }

    public String getName() {
        return getAttribute("name", String.class);
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
