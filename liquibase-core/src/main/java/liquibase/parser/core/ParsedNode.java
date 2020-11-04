package liquibase.parser.core;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.util.ISODateFormat;
import liquibase.util.StringUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Acts as a standard abstract syntax layer for changelogs defined in different formats.
 * {@link liquibase.parser.ChangeLogParser} implementations and other classes that work with multiple formats can create objects
 * directs or create instances of this class which can then be passed to the load() method of the object they want to configure.
 * For example, {@link liquibase.change.Change#load(ParsedNode, liquibase.resource.ResourceAccessor)}.
 * <p/>
 * ParsedNodes are a simple key/value structure with the following characteristics:
 * <ul>
 * <li>Keys include a namespace as well as the node name</li>
 * <li>There can be multiple children nodes with the same node namespace+name</li>
 * <li>There is an unkeyed "value" object in addition to the children nodes</li>
 * <li>The value node cannot be a ParsedNode. If you attempt to set value to be or contain a ParsedNode it will actually be set as a child</li>
 * </ul>
 */
public class ParsedNode {
    private String namespace;
    private String name;
    private List<ParsedNode> children = new ArrayList<>();
    private Object value;

    public ParsedNode(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
    }

    /**
     * Each node key contains both a namespace and a name which together identifies the node.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Each node key contains both a namespace and a name which together identifies the node.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the child ParsedNodes of this node. Returned list is unmodifiableList.
     */
    public List<ParsedNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Returns all child nodes with the given namespace and name.
     * If none match, an empty list is returned.
     * Returned list is unmodifiableList.
     */
    public List<ParsedNode> getChildren(String namespace, String nodename) {
        List<ParsedNode> returnList = new ArrayList<>();
        for (ParsedNode node : children) {
            if (nodeMatches(node, namespace, nodename)) {
                returnList.add(node);
            }
        }
        return Collections.unmodifiableList(returnList);
    }

    /**
     * Return the value associated with this node.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of this ParsedNode.
     * If the passed value is a ParsedNode, it is added as a child, not as the value.
     * If the passed value is a Map, it is converted to a ParsedNode and added as a child, not as the value.
     * If the passed value is a Collection, each object is added as a child if it is a ParsedNode or a Map.
     * If there are multiple simple values in a passed collection, value is set to a List. If there is a single value in a collection, value is set to the single value.
     */
    public ParsedNode setValue(Object value) throws ParsedNodeException {
        if (value instanceof ParsedNode) {
            this.addChild((ParsedNode) value);
        } else if (value instanceof Collection) {
            List newValue = new ArrayList();
            for (Object obj : ((Collection) value)) {
                if (obj instanceof Map) {
                    addChildren((Map) obj);
                } else if (obj instanceof ParsedNode) {
                    addChild(((ParsedNode) obj));
                } else {
                    newValue.add(obj);
                }
            }
            if (newValue.isEmpty()) {
                //do nothing
            } else if (newValue.size() == 1) {
                this.value = newValue.get(0);
            } else {
                this.value = newValue;
            }
        } else if (value instanceof Map) {
            addChildren(((Map) value));
        } else {
            this.value = value;
        }
        return this;
    }

    /**
     * Return the value associated with this node converted to the given type.
     *
     * @throws ParsedNodeException if the current value type cannot be converted
     */
    public <T> T getValue(Class<T> type) throws ParsedNodeException {
        return convertObject(value, type);
    }

    /**
     * Convenience method to add a new ParsedNode with the passed namespace/name and value
     */
    public ParsedNode addChild(String namespace, String nodeName, Object value) throws ParsedNodeException {
        addChild(createNode(namespace, nodeName).setValue(value));
        return this;
    }

    protected ParsedNode createNode(String namespace, String nodeName) {
        return new ParsedNode(namespace, nodeName);
    }

    /**
     * Adds the given ParsedNode as a child
     */
    public ParsedNode addChild(ParsedNode node) throws ParsedNodeException {
        children.add(node);
        return this;
    }

    /**
     * Adds the given map as children of this node.
     * If the passed map is empty, it is a no-op
     * For each key in the map, a new child is added with the key as the name and the value (with all {@link #setValue(Object)}) logic) is the value.
     */
    public ParsedNode addChildren(Map<String, Object> child) throws ParsedNodeException {
        if ((child == null) || child.isEmpty()) {
            return this; //do nothing
        }
        for (Map.Entry<String, Object> entry : child.entrySet()) {
            this.addChild(null, entry.getKey(), entry.getValue());
        }

        return this;
    }

    /**
     * Returns the ParsedNode defined by the given namespace and name.
     * @throws liquibase.parser.core.ParsedNodeException if multiple nodes match.
     */
    public ParsedNode getChild(String namespace, String name) throws ParsedNodeException {
        ParsedNode returnNode = null;
        for (ParsedNode node : children) {
            if (nodeMatches(node, namespace, name)) {
                if (returnNode != null) {
                    throw new ParsedNodeException("Multiple nodes match "+namespace+"/"+name);
                }
                returnNode = node;
            }
        }
        return returnNode;
    }

    public ParsedNode removeChild(String namespace, String name) throws ParsedNodeException {
        ListIterator<ParsedNode> iterator = children.listIterator();
        while (iterator.hasNext()) {
            ParsedNode node = iterator.next();
            if (nodeMatches(node, namespace, name)) {
                iterator.remove();
            }
        }
        return this;
    }

    protected boolean nodeMatches(ParsedNode node, String namespace, String nodename) {
        return namespaceMatches(node, namespace) && node.getName().equals(nodename);
    }

    protected boolean namespaceMatches(ParsedNode node, String namespace) {
        if (node.getNamespace() == null) {
            return namespace == null;
        }
        return node.getNamespace().equals(namespace);
    }

    /**
     * Convenience method for {@link #getChildValue(String, String, Class)} but returns the passed defaultValue if the given node is null or not defined.
     */
    public <T> T getChildValue(String namespace, String nodename, T defaultValue) throws ParsedNodeException {
        T value = (T) getChildValue(namespace, nodename, defaultValue.getClass());
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Returns the value of the given node, converted to the passed type.
     * @throws liquibase.parser.core.ParsedNodeException if there is an error converting the value
     */
    public <T> T getChildValue(String namespace, String nodename, Class<T> type) throws ParsedNodeException {
        Object rawValue = getChildValue(namespace, nodename);
        if (rawValue == null) {
            return null;
        }
        return convertObject(rawValue, type);
    }

    protected <T> T convertObject(Object rawValue, Class<T> type) throws ParsedNodeException {
        if (rawValue == null) {
            return null;
        }
        if (type.isAssignableFrom(rawValue.getClass())) {
            return (T) rawValue;
        }

        try {
            if (type.equals(String.class)) {
                return (T) rawValue.toString();
            } else if (type.equals(Integer.class)) {
                return (T) Integer.valueOf(rawValue.toString());
            } else if (type.equals(Short.class)) {
                return (T) Short.valueOf(rawValue.toString());
            } else if (type.equals(Float.class)) {
                return (T) Float.valueOf(rawValue.toString());
            } else if (type.equals(Double.class)) {
                return (T) Double.valueOf(rawValue.toString());
            } else if (type.equals(Long.class)) {
                return (T) Long.valueOf(rawValue.toString());
            } else if (type.equals(BigInteger.class)) {
                return (T) new BigInteger(rawValue.toString());
            } else if (type.equals(BigDecimal.class)) {
                return (T) new BigDecimal(rawValue.toString());
            } else if (type.equals(Boolean.class) && (rawValue instanceof String)) {
                return (T) Boolean.valueOf(rawValue.toString());
            } else if (type.isAssignableFrom(Date.class)) {
                return (T) new ISODateFormat().parse(rawValue.toString());
            } else if (type.equals(SequenceNextValueFunction.class)) {
                return (T) new SequenceNextValueFunction(rawValue.toString());
            } else if (type.equals(SequenceCurrentValueFunction.class)) {
                return (T) new SequenceCurrentValueFunction(rawValue.toString());
            } else if (type.equals(DatabaseFunction.class)) {
                return (T) new DatabaseFunction(rawValue.toString());
            } else if (type.isEnum()) {
                return (T) Enum.valueOf((Class<Enum>)type, rawValue.toString());
            } else {
                throw new UnexpectedLiquibaseException("Cannot convert " + rawValue.getClass().getName() + " '" + rawValue + "' to " + type.getName());
            }
        } catch (Exception e) {
            if (e instanceof UnexpectedLiquibaseException) {
                throw (UnexpectedLiquibaseException) e;
            }
            throw new ParsedNodeException(e);
        }
    }

    /**
     * Returns the value of the given node with no conversion attempted.
     */
    public Object getChildValue(String namespace, String nodename) throws ParsedNodeException {
        ParsedNode child = getChild(namespace, nodename);
        if (child == null) {
            return null;
        }
        return child.getValue();
    }

    @Override
    public String toString() {
        String string = name;
        if (!children.isEmpty()) {
            string += "[" + StringUtil.join(children, ",", new StringUtil.ToStringFormatter(), true) + "]";
        }
        if (value != null) {
            String valueString;
            if (value instanceof Collection) {
                valueString = "("+ StringUtil.join(((Collection) value), ",", new StringUtil.ToStringFormatter(), true)+")";
            } else {
                valueString = value.toString();
            }
            string += "="+valueString;
        }
        return string;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ParsedNode) && this.toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
