package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;

import java.util.ArrayList;
import java.util.List;

/**
 * A container of columns. Usually a table or view.
 */
public abstract class Relation extends AbstractDatabaseObject {

    public String remarks;

    protected Relation() {
    }

    protected Relation(ObjectName name) {
        super(name);
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[]{
                getSchema()
        };

    }

    /**
     * @return Returns the schema.
     */
    @Override
    public Schema getSchema() {
        return null;
    }

    public int compareTo(Object o) {
        return this.getName().compareTo(((Relation) o).getName());
    }

}
