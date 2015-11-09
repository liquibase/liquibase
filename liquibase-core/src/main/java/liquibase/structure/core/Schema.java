package liquibase.structure.core;

import liquibase.CatalogAndSchema;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectReference;

import java.util.*;

public class Schema extends AbstractDatabaseObject {

    public ObjectReference catalog;
    public Boolean isDefault;


    public Schema() {
    }

    public Schema(String name) {
        super(name);
    }

    public Schema(ObjectReference nameAndContainer) {
        super(nameAndContainer);
    }

    public Schema(ObjectReference catalog, String schemaName) {
        this(schemaName);
        this.catalog = catalog;
    }

    @Override
    public String toString() {
        return toString(catalog, name);
    }
}
