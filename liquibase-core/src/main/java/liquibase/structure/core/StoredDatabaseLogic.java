package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtil;

public abstract class StoredDatabaseLogic<T extends StoredDatabaseLogic> extends AbstractDatabaseObject {
    @Override
    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[]{
                getSchema()
        };
    }

    @Override
    public String getName() {
        return getAttribute("name", String.class);
    }

    @Override
    public T setName(String name) {
        setAttribute("name", name);
        return (T) this;
    }

    @Override
    public Schema getSchema() {
        return getAttribute("schema", Schema.class);
    }

    public T setSchema(Schema schema) {
        setAttribute("schema", schema);
        return (T) this;
    }

    public Boolean isValid() {
        return getAttribute("valid", Boolean.class);
    }

    public T setValid(Boolean valid) {
        setAttribute("valid", valid);
        return (T) this;
    }

    public String getBody() {
        return getAttribute("body", String.class);
    }

    public T setBody(String body) {
        setAttribute("body", body);
        return (T) this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if ((obj == null) || (getClass() != obj.getClass())) return false;

        StoredDatabaseLogic that = (StoredDatabaseLogic) obj;

        if ((this.getSchema() != null) && (that.getSchema() != null)) {
            boolean schemasEqual = StringUtil.trimToEmpty(this.getSchema().getName()).equalsIgnoreCase(StringUtil.trimToEmpty(that.getSchema().getName()));
            if (!schemasEqual) {
                return false;
            }
        }

        return getName().equalsIgnoreCase(that.getName());
    }

    @Override
    public int hashCode() {
        return StringUtil.trimToEmpty(this.getName()).toLowerCase().hashCode();
    }
}
