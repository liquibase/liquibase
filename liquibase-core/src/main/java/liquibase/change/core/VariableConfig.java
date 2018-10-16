package liquibase.change.core;


import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.AbstractLiquibaseSerializable;
import liquibase.util.ObjectUtil;

/**
 * The configuration loadData uses to represent a 'variable' type column (i.e. supplies data to a select, is not
 * actually loaded directly into a column in the table...)
 */
public class VariableConfig extends AbstractLiquibaseSerializable {

    private String name;

    /**
     * The name of the column.
     */
    public String getName() {
        return name;
    }

    public VariableConfig setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getSerializedObjectName() {
        return "variable";
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        return SerializationType.NAMED_FIELD;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        for (ParsedNode child : parsedNode.getChildren()) {
            if (!ObjectUtil.hasProperty(this, child.getName())) {
                throw new ParsedNodeException("Unexpected node: "+child.getName());
            }
        }

        name = parsedNode.getChildValue(null, "name", String.class);
    }
}
