package liquibase.serializer.core.xml;

import liquibase.change.Change;
import liquibase.change.ChangeProperty;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.TextNode;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.util.ISODateFormat;
import liquibase.util.StringUtils;
import liquibase.util.XMLUtil;
import org.w3c.dom.*;

import java.lang.reflect.Field;
import java.util.*;

public class XMLChangeLogSerializer implements ChangeLogSerializer {

    private Document currentChangeLogFileDOM;

    public XMLChangeLogSerializer() {
    }

    protected XMLChangeLogSerializer(Document currentChangeLogFileDOM) {
        this.currentChangeLogFileDOM = currentChangeLogFileDOM;
    }

    public void setCurrentChangeLogFileDOM(Document currentChangeLogFileDOM) {
        this.currentChangeLogFileDOM = currentChangeLogFileDOM;
    }

    public String[] getValidFileExtensions() {
        return new String[] {"xml"};
    }


    public String serialize(DatabaseChangeLog databaseChangeLog) {
        return null; //todo
    }

    public String serialize(Change change) {
        StringBuffer buffer = new StringBuffer();
        nodeToStringBuffer(createNode(change), buffer);
        return buffer.toString();
    }

    public String serialize(SqlVisitor visitor) {
        StringBuffer buffer = new StringBuffer();
        nodeToStringBuffer(createNode(visitor), buffer);
        return buffer.toString();
    }

    public String serialize(ColumnConfig columnConfig) {
        StringBuffer buffer = new StringBuffer();
        nodeToStringBuffer(createNode(columnConfig), buffer);
        return buffer.toString();
    }

    public String serialize(ChangeSet changeSet) {
        StringBuffer buffer = new StringBuffer();
        nodeToStringBuffer(createNode(changeSet), buffer);
        return buffer.toString();

    }

    public Element createNode(SqlVisitor visitor) {
        Element node = currentChangeLogFileDOM.createElementNS(XMLChangeLogSAXParser.getDatabaseChangeLogNameSpace(), visitor.getName());
        try {
            List<Field> allFields = new ArrayList<Field>();
            Class classToExtractFieldsFrom = visitor.getClass();
            while (!classToExtractFieldsFrom.equals(Object.class)) {
                allFields.addAll(Arrays.asList(classToExtractFieldsFrom.getDeclaredFields()));
                classToExtractFieldsFrom = classToExtractFieldsFrom.getSuperclass();
            }

            for (Field field : allFields) {
                field.setAccessible(true);
                ChangeProperty changePropertyAnnotation = field.getAnnotation(ChangeProperty.class);
                if (changePropertyAnnotation != null && !changePropertyAnnotation.includeInSerialization()) {
                    continue;
                }
                if (field.getName().equals("serialVersionUID")) {
                    continue;
                }
                if (field.getName().equals("$VRc")) { //from emma
                    continue;
                }


                String propertyName = field.getName();
                Object value = field.get(visitor);
                if (value != null) {
                    node.setAttribute(propertyName, value.toString());
                }
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

        return node;
    }

    public Element createNode(Change change) {
        Element node = currentChangeLogFileDOM.createElementNS(XMLChangeLogSAXParser.getDatabaseChangeLogNameSpace(), change.getChangeMetaData().getName());
        try {
            List<Field> allFields = new ArrayList<Field>();
            Class classToExtractFieldsFrom = change.getClass();
            while (!classToExtractFieldsFrom.equals(Object.class)) {
                allFields.addAll(Arrays.asList(classToExtractFieldsFrom.getDeclaredFields()));
                classToExtractFieldsFrom = classToExtractFieldsFrom.getSuperclass();
            }

            for (Field field : allFields) {
                field.setAccessible(true);
                ChangeProperty changePropertyAnnotation = field.getAnnotation(ChangeProperty.class);
                if (changePropertyAnnotation != null && !changePropertyAnnotation.includeInSerialization()) {
                    continue;
                }
                if (field.getName().equals("serialVersionUID")) {
                    continue;
                }
                if (field.getName().equals("$VRc")) { //from emma
                    continue;
                }
                
                // String properties annotated with @TextNode are serialized as a child node
                TextNode textNodeAnnotation = field.getAnnotation(TextNode.class);
                if (textNodeAnnotation != null) {
                    String textNodeContent = (String) field.get(change);
                    node.appendChild(createNode(textNodeAnnotation.nodeName(), textNodeContent));
                    continue;
                }
                
                String propertyName = field.getName();
                if (field.getType().equals(ColumnConfig.class)) {
                    node.appendChild(createNode((ColumnConfig) field.get(change)));
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    for (Object object : (Collection) field.get(change)) {
                        if (object instanceof ColumnConfig) {
                            node.appendChild(createNode((ColumnConfig) object));
                        }
                    }
                } else {
                    Object value = field.get(change);
                    if (value != null) {
                        if (propertyName.equals("procedureBody")
                                || propertyName.equals("sql")
                                || propertyName.equals("selectQuery")) {
                            node.setTextContent(value.toString());
                        } else {
                            node.setAttribute(propertyName, value.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

        return node;
    }

    // create a XML node with nodeName and simple text content
    public Element createNode(String nodeName, String nodeContent) {
        Element element = currentChangeLogFileDOM.createElementNS(XMLChangeLogSAXParser.getDatabaseChangeLogNameSpace(), nodeName);
        element.setTextContent(nodeContent);
        return element;
    }
    
    public Element createNode(ColumnConfig columnConfig) {
        Element element = currentChangeLogFileDOM.createElementNS(XMLChangeLogSAXParser.getDatabaseChangeLogNameSpace(), "column");
        if (columnConfig.getName() != null) {
            element.setAttribute("name", columnConfig.getName());
        }
        if (columnConfig.getType() != null) {
            element.setAttribute("type", columnConfig.getType());
        }

        if (columnConfig.getDefaultValue() != null) {
            element.setAttribute("defaultValue", columnConfig.getDefaultValue());
        }
        if (columnConfig.getDefaultValueNumeric() != null) {
            element.setAttribute("defaultValueNumeric", columnConfig.getDefaultValueNumeric().toString());
        }
        if (columnConfig.getDefaultValueDate() != null) {
            element.setAttribute("defaultValueDate", new ISODateFormat().format(columnConfig.getDefaultValueDate()));
        }
        if (columnConfig.getDefaultValueBoolean() != null) {
            element.setAttribute("defaultValueBoolean", columnConfig.getDefaultValueBoolean().toString());
        }

        if (columnConfig.getValue() != null) {
            element.setAttribute("value", columnConfig.getValue());
        }
        if (columnConfig.getValueNumeric() != null) {
            element.setAttribute("valueNumeric", columnConfig.getValueNumeric().toString());
        }
        if (columnConfig.getValueBoolean() != null) {
            element.setAttribute("valueBoolean", columnConfig.getValueBoolean().toString());
        }
        if (columnConfig.getValueDate() != null) {
            element.setAttribute("valueDate", new ISODateFormat().format(columnConfig.getValueDate()));
        }
        if (StringUtils.trimToNull(columnConfig.getRemarks()) != null) {
            element.setAttribute("remarks", columnConfig.getRemarks());
        }

        if (columnConfig.isAutoIncrement() != null && columnConfig.isAutoIncrement()) {
            element.setAttribute("autoIncrement", "true");
        }

        ConstraintsConfig constraints = columnConfig.getConstraints();
        if (constraints != null) {
            Element constraintsElement = currentChangeLogFileDOM.createElementNS(XMLChangeLogSAXParser.getDatabaseChangeLogNameSpace(), "constraints");
            if (constraints.getCheck() != null) {
                constraintsElement.setAttribute("check", constraints.getCheck());
            }
            if (constraints.getForeignKeyName() != null) {
                constraintsElement.setAttribute("foreignKeyName", constraints.getForeignKeyName());
            }
            if (constraints.getReferences() != null) {
                constraintsElement.setAttribute("references", constraints.getReferences());
            }
            if (constraints.isDeferrable() != null) {
                constraintsElement.setAttribute("deferrable", constraints.isDeferrable().toString());
            }
            if (constraints.isDeleteCascade() != null) {
                constraintsElement.setAttribute("deleteCascade", constraints.isDeleteCascade().toString());
            }
            if (constraints.isInitiallyDeferred() != null) {
                constraintsElement.setAttribute("initiallyDeferred", constraints.isInitiallyDeferred().toString());
            }
            if (constraints.isNullable() != null) {
                constraintsElement.setAttribute("nullable", constraints.isNullable().toString());
            }
            if (constraints.isPrimaryKey() != null) {
                constraintsElement.setAttribute("primaryKey", constraints.isPrimaryKey().toString());
            }
            if (constraints.isUnique() != null) {
                constraintsElement.setAttribute("unique", constraints.isUnique().toString());
            }

            if (constraints.getUniqueConstraintName() != null) {
                constraintsElement.setAttribute("uniqueConstraintName", constraints.getUniqueConstraintName());
            }

            if (constraints.getPrimaryKeyName() != null) {
                constraintsElement.setAttribute("primaryKeyName", constraints.getPrimaryKeyName());
            }

	        if (constraints.getPrimaryKeyTablespace() != null) {
                constraintsElement.setAttribute("primaryKeyTablespace", constraints.getPrimaryKeyTablespace());
            }
            element.appendChild(constraintsElement);
        }

        return element;
    }

    public Element createNode(ChangeSet changeSet) {
        Element node = currentChangeLogFileDOM.createElementNS(XMLChangeLogSAXParser.getDatabaseChangeLogNameSpace(), "changeSet");
        node.setAttribute("id", changeSet.getId());
        node.setAttribute("author", changeSet.getAuthor());

        if (changeSet.isAlwaysRun()) {
            node.setAttribute("runAlways", "true");
        }

        if (changeSet.isRunOnChange()) {
            node.setAttribute("runOnChange", "true");
        }

        if (changeSet.getFailOnError() != null) {
            node.setAttribute("failOnError", changeSet.getFailOnError().toString());
        }

        if (changeSet.getContexts() != null && changeSet.getContexts().size() > 0) {
            StringBuffer contextString = new StringBuffer();
            for (String context : changeSet.getContexts()) {
                contextString.append(context).append(",");
            }
            node.setAttribute("context", contextString.toString().replaceFirst(",$", ""));
        }

        if (changeSet.getDbmsSet() != null && changeSet.getDbmsSet().size() > 0) {
            StringBuffer dbmsString = new StringBuffer();
            for (String dbms : changeSet.getDbmsSet()) {
                dbmsString.append(dbms).append(",");
            }
            node.setAttribute("dbms", dbmsString.toString().replaceFirst(",$", ""));
        }

        if (StringUtils.trimToNull(changeSet.getComments()) != null) {
            Element commentsElement = currentChangeLogFileDOM.createElementNS(XMLChangeLogSAXParser.getDatabaseChangeLogNameSpace(), "comment");
            Text commentsText = currentChangeLogFileDOM.createTextNode(changeSet.getComments());
            commentsElement.appendChild(commentsText);
            node.appendChild(commentsElement);
        }


        for (Change change : changeSet.getChanges()) {
            node.appendChild(createNode(change));
        }
        return node;
    }


    public Element createNode(CustomChangeWrapper change) {
        Element customElement = currentChangeLogFileDOM.createElementNS(XMLChangeLogSAXParser.getDatabaseChangeLogNameSpace(), "custom");
        customElement.setAttribute("class", change.getClassName());

        for (String param : change.getParams()) {
            Element paramElement = currentChangeLogFileDOM.createElementNS(XMLChangeLogSAXParser.getDatabaseChangeLogNameSpace(), "param");
            paramElement.setAttribute("name", param);
            paramElement.setAttribute("value", change.getParamValues().get(param));

            customElement.appendChild(paramElement);
        }

        return customElement;
    }

    /*
     * Creates a {@link String} using the XML element representation of this
     * change
     *
     * @param node the {@link Element} associated to this change
     * @param buffer a {@link StringBuffer} object used to hold the {@link String}
     *               representation of the change
     */
    private void nodeToStringBuffer(Node node, StringBuffer buffer) {
        buffer.append("<").append(node.getNodeName());
        SortedMap<String, String> attributeMap = new TreeMap<String, String>();
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            attributeMap.put(attribute.getNodeName(), attribute.getNodeValue());
        }
        for (Map.Entry entry : attributeMap.entrySet()) {
            String value = (String) entry.getValue();
            if (value != null) {
                buffer.append(" ").append(entry.getKey()).append("=\"").append(value).append("\"");
            }
        }
        buffer.append(">").append(StringUtils.trimToEmpty(XMLUtil.getTextContent(node)));
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                nodeToStringBuffer(((Element) childNode), buffer);
            }
        }
        buffer.append("</").append(node.getNodeName()).append(">");
    }

}
