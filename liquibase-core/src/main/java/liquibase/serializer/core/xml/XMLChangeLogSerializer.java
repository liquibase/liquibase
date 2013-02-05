package liquibase.serializer.core.xml;

import liquibase.change.*;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.parser.core.xml.LiquibaseEntityResolver;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.util.ISODateFormat;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;
import liquibase.util.XMLUtil;
import liquibase.util.xml.DefaultXmlWriter;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
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
        return new String[]{"xml"};
    }


    public String serialize(DatabaseChangeLog databaseChangeLog) {
        return null; //todo
    }

    public String serialize(LiquibaseSerializable object) {
        StringBuffer buffer = new StringBuffer();
        nodeToStringBuffer(createNode(object), buffer);
        return buffer.toString();
    }

    public void write(List<ChangeSet> changeSets, OutputStream out) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        documentBuilder.setEntityResolver(new LiquibaseEntityResolver());

        Document doc = documentBuilder.newDocument();
        Element changeLogElement = doc.createElementNS(XMLChangeLogSAXParser.getDatabaseChangeLogNameSpace(), "databaseChangeLog");

        changeLogElement.setAttribute("xmlns", XMLChangeLogSAXParser.getDatabaseChangeLogNameSpace());
        changeLogElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        changeLogElement.setAttribute("xsi:schemaLocation", "http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-" + XMLChangeLogSAXParser.getSchemaVersion() + ".xsd");

        doc.appendChild(changeLogElement);
        setCurrentChangeLogFileDOM(doc);

        for (ChangeSet changeSet : changeSets) {
            doc.getDocumentElement().appendChild(createNode(changeSet));
        }

        new DefaultXmlWriter().write(doc, out);
    }

    public void append(ChangeSet changeSet, File changeLogFile) throws IOException {
        FileInputStream in = new FileInputStream(changeLogFile);
        String existingChangeLog = StreamUtil.getStreamContents(in);
        in.close();

        FileOutputStream out = new FileOutputStream(changeLogFile);

        if (!existingChangeLog.contains("</databaseChangeLog>")) {
            write(Arrays.asList(changeSet), out);
        } else {
            existingChangeLog = existingChangeLog.replaceFirst("</databaseChangeLog>", serialize(changeSet) + "\n</databaseChangeLog>");

            StreamUtil.copy(new ByteArrayInputStream(existingChangeLog.getBytes()), out);
        }
        out.flush();
        out.close();
    }

    public Element createNode(LiquibaseSerializable object) {
        Element node = currentChangeLogFileDOM.createElementNS(XMLChangeLogSAXParser.getDatabaseChangeLogNameSpace(), object.getSerializedObjectName());

        for (String field : object.getSerializableFields()) {
            setValueOnNode(node, field, object.getSerializableFieldValue(field), object.getSerializableFieldType(field));
        }

        return node;
    }

    private void setValueOnNode(Element node, String objectName, Object value, LiquibaseSerializable.SerializationType serializationType) {
        if (value == null) {
            return;
        }

        if (value instanceof Collection) {
            for (Object child : (Collection) value) {
                setValueOnNode(node, objectName, child, serializationType);
            }
        } else if (value instanceof Map) {
            for (Map.Entry entry : (Set<Map.Entry>) ((Map) value).entrySet()) {
                Element mapNode = currentChangeLogFileDOM.createElementNS(XMLChangeLogSAXParser.getDatabaseChangeLogNameSpace(), objectName);
                setValueOnNode(mapNode, (String) entry.getKey(), entry.getValue(), serializationType);
            }
        } else if (value instanceof LiquibaseSerializable) {
            node.appendChild(createNode((LiquibaseSerializable) value));
        } else {
            if (serializationType.equals(LiquibaseSerializable.SerializationType.NESTED_OBJECT)) {
                node.appendChild(createNode(objectName, value.toString()));
            } else if (serializationType.equals(LiquibaseSerializable.SerializationType.DIRECT_VALUE)) {
                node.setTextContent(value.toString());
            } else {
                node.setAttribute(objectName, value.toString());
            }
        }
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
        if (columnConfig.getDefaultValueComputed() != null) {
            element.setAttribute("defaultValueComputed", columnConfig.getDefaultValueComputed().toString());
        }
        if (columnConfig.getDefaultValueSequenceNext() != null) {
            element.setAttribute("defaultValueSequenceNext", columnConfig.getDefaultValueSequenceNext().toString());
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
        if (columnConfig.getValueComputed() != null) {
            element.setAttribute("valueComputed", columnConfig.getValueComputed().toString());
        }
        if (columnConfig.getValueSequenceNext() != null) {
            element.setAttribute("valueSequenceNext", columnConfig.getValueSequenceNext().toString());
        }
        if (columnConfig.getValueSequenceCurrent() != null) {
            element.setAttribute("valueSequenceNext", columnConfig.getValueSequenceCurrent().toString());
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
            if (constraints.getCheckConstraint() != null) {
                constraintsElement.setAttribute("checkConstraint", constraints.getCheckConstraint());
            }
            if (constraints.getForeignKeyName() != null) {
                constraintsElement.setAttribute("foreignKeyName", constraints.getForeignKeyName());
            }
            if (constraints.getReferences() != null) {
                constraintsElement.setAttribute("references", constraints.getReferences());
            }
            if (constraints.getReferencedTableName() != null) {
                constraintsElement.setAttribute("referencedTableName", constraints.getReferencedTableName());
            }
            if (constraints.getReferencedColumnNames() != null) {
                constraintsElement.setAttribute("referencedTableName", constraints.getReferencedColumnNames());
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
