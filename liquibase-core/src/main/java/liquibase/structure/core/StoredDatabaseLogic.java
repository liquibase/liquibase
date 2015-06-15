package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;

public abstract class StoredDatabaseLogic<T extends StoredDatabaseLogic> extends AbstractDatabaseObject {

    public StoredDatabaseLogic() {
    }

    public StoredDatabaseLogic(ObjectName name) {
        super(name);
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[]{
                getSchema()
        };
    }

    @Override
    public Schema getSchema() {
        return get("schema", Schema.class);
    }

    public T setSchema(Schema schema) {
        set("schema", schema);
        return (T) this;
    }

    public Boolean isValid() {
        return get("valid", Boolean.class);
    }

    public T setValid(Boolean valid) {
        set("valid", valid);
        return (T) this;
    }

    public String getBody() {
        return get("body", String.class);
    }

    public T setBody(String body) {
        set("body", body);
        return (T) this;
    }
}
