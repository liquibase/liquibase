package liquibase.parser.core;

import liquibase.exception.ChangeLogParseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.ISODateFormat;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;

public class ParsedNode {
    private String namespace;
    private String nodeName;
    private List<ParsedNode> children = new ArrayList<ParsedNode>();
    private Object value;

    public ParsedNode(String namespace, String nodeName) {
        this.namespace = namespace;
        this.nodeName = nodeName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getNodeName() {
        return nodeName;
    }

    public List<ParsedNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public List<ParsedNode> getChildren(String namespace, String nodename) {
        List<ParsedNode> returnList = new ArrayList<ParsedNode>();
        for (ParsedNode node : children) {
            if (nodeMatches(node, namespace, nodename)) {
                returnList.add(node);
            }
        }
        return Collections.unmodifiableList(returnList);
    }

    public Object getValue() {
        return value;
    }

    public <T> T getValue(Class<T> type) {
        return (T) value;
    }

    public ParsedNode setValue(Object value) throws ChangeLogParseException {
        if (value instanceof Collection) {
            List newValue = new ArrayList();
            for (Object obj : ((Collection) value)) {
                if (obj instanceof Map) {
                    newValue.add(mapToChangeLogNode((Map) obj));
                } else {
                    newValue.add(obj);
                }
            }
            this.value = newValue;
        } else if (value instanceof Map) {
            this.value = mapToChangeLogNode(((Map) value));
        } else {
            this.value = value;
        }
        return this;
    }

    public ParsedNode addChild(String namespace, String nodeName, Object value) throws ChangeLogParseException {
        addChild(new ParsedNode(namespace, nodeName).setValue(value));
        return this;
    }

    public ParsedNode addChild(ParsedNode node) throws ChangeLogParseException  {
        children.add(node);
        return this;
    }

    public ParsedNode addChildren(Object children) throws ChangeLogParseException {
        if (children instanceof Map) {
            for (Map.Entry<String, Object> child : ((Map<String, Object>) children).entrySet()) {
                if (child.getValue() instanceof Map || child.getValue() instanceof Collection) {
                    String childKey = child.getKey();
                    ParsedNode childNode = new ParsedNode(null, childKey);

                    childNode.addChildren(child.getValue());
                    addChild(childNode);
                } else {
                    addChild(null, child.getKey(), child.getValue());
                }
            }
        } else if (children instanceof Collection) {
            for (Object child : (Collection) children) {
                if (child instanceof Map) {
                    child = mapToChangeLogNode((Map) child);
                }

                if (value == null) {
                    value = new ArrayList();
                }
                ((Collection) value).add(child);
            }
        } else {
            setValue(children);
        }
        return this;
    }

    protected ParsedNode mapToChangeLogNode(Map child) throws ChangeLogParseException {
        if (((Map) child).size() != 1) {
            throw new ChangeLogParseException("Maps in lists need to have one and only one key");
        }
        String childKey = (String) ((Map) child).keySet().iterator().next();
        ParsedNode childNode = new ParsedNode(null, childKey);
        childNode.addChildren(((Map) child).get(childKey));
        return childNode;
    }


    public ParsedNode getChild(String namespace, String name) {
        for (ParsedNode node : children) {
            if (nodeMatches(node, namespace, name)) {
                return node;
            }
        }
        return null;
    }

    protected boolean nodeMatches(ParsedNode node, String namespace, String nodename) {
        return namespaceMatches(node, namespace) && node.getNodeName().equals(nodename);
    }

    protected boolean namespaceMatches(ParsedNode node, String namespace) {
        if (node.getNamespace() == null) {
            return namespace == null;
        }
        return node.getNamespace().equals(namespace);
    }

    public <T> T getChildValue(String namespace, String nodename, T defaultValue) throws ParseException {
        T value = (T) getChildValue(namespace, nodename, defaultValue.getClass());
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public <T> T getChildValue(String namespace, String nodename, Class<T> type) throws ParseException {
        Object rawValue = getChildValue(namespace, nodename);
        if (rawValue == null) {
            return null;
        }
        if (type.isAssignableFrom(rawValue.getClass())) {
            return (T) rawValue;
        }

        if (type.equals(String.class)) {
            return (T) rawValue.toString();
        } else if (type.equals(Integer.class)) {
            return (T) Integer.valueOf(rawValue.toString());
        } else if (type.equals(Float.class)) {
            return (T) Float.valueOf(rawValue.toString());
        } else if (type.equals(Double.class)) {
            return (T) Double.valueOf(rawValue.toString());
        } else if (type.equals(Long.class)) {
            return (T) Long.valueOf(rawValue.toString());
        } else if (type.equals(BigInteger.class)) {
            return (T) BigInteger.valueOf(Long.valueOf(rawValue.toString()));
        } else if (type.equals(Boolean.class) && rawValue instanceof String) {
            return (T) Boolean.valueOf(rawValue.toString());
        } else if (type.isAssignableFrom(Date.class)) {
            return (T) new ISODateFormat().parse(rawValue.toString());
        } else {
            throw new UnexpectedLiquibaseException("Cannot convert "+rawValue.getClass().getName()+" '"+rawValue+"' to "+type.getName());
        }
    }

    public Object getChildValue(String namespace, String nodename) {
        ParsedNode child = getChild(namespace, nodename);
        if (child == null) {
            return null;
        }
        return child.getValue();
    }

    @Override
    public String toString() {
        String string = nodeName;
        if (children.size() > 0) {
            string += "["+ StringUtils.join(children, ",", new StringUtils.ToStringFormatter(), true)+"]";
        }
        if (value != null) {
            String valueString;
            if (value instanceof Collection) {
                valueString = "("+StringUtils.join(((Collection) value), ",", new StringUtils.ToStringFormatter(), true)+")";
            } else {
                valueString = value.toString();
            }
            string += "="+valueString;
        }
        return string;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ParsedNode && this.toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
