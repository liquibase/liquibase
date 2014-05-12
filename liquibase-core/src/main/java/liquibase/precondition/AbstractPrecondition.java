package liquibase.precondition;

import liquibase.exception.SetupException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.util.ObjectUtil;

import java.util.Set;

public abstract class AbstractPrecondition implements Precondition {

    @Override
    public String getSerializedObjectName() {
        return getName();
    }

    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException, SetupException {
        for (ParsedNode childNode : parsedNode.getChildren()) {
            try {
                if (ObjectUtil.hasWriteProperty(this, childNode.getName())) {
                    Object value = childNode.getValue();
                    if (value != null) {
                        value = value.toString();
                    }
                    ObjectUtil.setProperty(this, childNode.getName(), (String) value);
                }
            } catch (Exception e) {
                throw new SetupException("Error setting property", e);
            }
        }
    }

    @Override
    public ParsedNode serialize() {
        return null;
    }

    @Override
    public Set<String> getSerializableFields() {
        return null;
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        return null;
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        return null;
    }
}
