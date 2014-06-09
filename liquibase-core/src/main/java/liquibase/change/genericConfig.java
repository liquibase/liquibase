package liquibase.change;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.AbstractLiquibaseSerializable;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.ReflectionSerializer;
import liquibase.util.StringUtils;

import java.util.Set;

/**
 * The standard configuration used by Change classes to represent a constraints on a column.
 */
public class genericConfig extends AbstractLiquibaseSerializable {

    private String configName;
    private Set<String> serializableFields;
    private Map<String,String> attributes;
    
    
    public genericConfig( String name ) {
        setName( name );
        serializableFields = new HashSet<String>();
        attributes = new HashMap<String,String>();
    }
    
    @Override
    public Set<String> getSerializableFields() {        
        return this.serializableFields;
    }
    
    public void setSerializableField( String serializableField ) {        
        this.serializableFields.add(serializableField);
    }
    
    public void setSerializableFields( Set<String> serializableFields ) {        
        this.serializableFields = serializableFields;
    }
    
    @Override
    public Object getSerializableFieldValue(String field) {
        return attributes.get(field);
    }
     
    public void setSerializableFieldValue(String field, String value) {
        attributes.put(field, value);
    }
    
    /**
     * Returns the name to use for the primary key constraint. Returns null if not specified
     */
    public String getName() {
        return configName;
    }

    public genericConfig setName(String configName) {
        this.configName = configName;
        return this;
    }

    @Override
    public String getSerializedObjectName() {
        return configName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
    
    
    @Override
    public SerializationType getSerializableFieldType(String field) {
        return SerializationType.NAMED_FIELD;
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        throw new RuntimeException("TODO");
    }
}