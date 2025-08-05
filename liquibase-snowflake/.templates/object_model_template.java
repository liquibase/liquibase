package liquibase.database.object;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;

import java.util.Objects;

/**
 * Represents a Snowflake ${ObjectType} database object.
 * Generated from universal template with TDD enforcement.
 */
public class ${ObjectType} extends AbstractDatabaseObject {

    private String name;
    private Schema schema;
    
    // Properties will be added via TDD micro-cycles
    ${PropertyDeclarations}

    public ${ObjectType}() {
        super();
    }

    public ${ObjectType}(String name) {
        this();
        setName(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ${ObjectType} setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    public ${ObjectType} setSchema(Schema schema) {
        this.schema = schema;
        return this;
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return schema != null ? new DatabaseObject[] { schema } : null;
    }

    // Property getters/setters will be added via TDD micro-cycles
    ${PropertyMethods}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ${ObjectType} that = (${ObjectType}) obj;
        return Objects.equals(name, that.name) &&
               Objects.equals(schema, that.schema)${PropertyEqualsChecks};
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, schema${PropertyHashFields});
    }

    @Override
    public String toString() {
        return "${ObjectType}{" +
               "name='" + name + '\'' +
               ", schema=" + schema +
               ${PropertyToStringFields}
               '}';
    }
}