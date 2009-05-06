package liquibase.changelog.parser.xml;

import liquibase.ChangeSet;
import liquibase.change.*;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.changelog.ChangeLogSerializer;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.ISODateFormat;
import liquibase.util.StringUtils;
import liquibase.util.XMLUtil;
import org.w3c.dom.*;

import java.lang.reflect.Field;
import java.sql.DatabaseMetaData;
import java.util.*;

public class XMLChangeLogSerializer implements ChangeLogSerializer {

    private Document currentChangeLogFileDOM;

    public XMLChangeLogSerializer(Document currentChangeLogFileDOM) {
        this.currentChangeLogFileDOM = currentChangeLogFileDOM;
    }

    public String serialize(Change change) {
        StringBuffer buffer = new StringBuffer();
        nodeToStringBuffer(createNode(change), buffer);
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

    public Element createNode(Change change) {
        Element node = currentChangeLogFileDOM.createElement(change.getChangeMetaData().getName());
        try {
            List<Field> allFields = new ArrayList<Field>();
            Class classToExtractFieldsFrom = change.getClass();
            while (!classToExtractFieldsFrom.equals(Object.class)) {
                allFields.addAll(Arrays.asList(classToExtractFieldsFrom.getDeclaredFields()));
                classToExtractFieldsFrom = classToExtractFieldsFrom.getSuperclass();
            }

            for (Field field : allFields) {
                field.setAccessible(true);
                if (field.getAnnotation(ChangeMetaDataField.class) != null) {
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

    public Element createNode(ColumnConfig columnConfig) {
        Element element = currentChangeLogFileDOM.createElement("column");
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
            Element constraintsElement = currentChangeLogFileDOM.createElement("constraints");
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
            element.appendChild(constraintsElement);
        }

        return element;
    }

    public Element createNode(ChangeSet changeSet) {
        Element node = currentChangeLogFileDOM.createElement("changeSet");
        node.setAttribute("id", changeSet.getId());
        node.setAttribute("author", changeSet.getAuthor());

        if (changeSet.isAlwaysRun()) {
            node.setAttribute("alwaysRun", "true");
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
            Element commentsElement = currentChangeLogFileDOM.createElement("comment");
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
        Element customElement = currentChangeLogFileDOM.createElement("custom");
        customElement.setAttribute("class", change.getClassName());

        for (String param : change.getParams()) {
            Element paramElement = currentChangeLogFileDOM.createElement("param");
            paramElement.setAttribute("name", param);
            paramElement.setAttribute("value", change.getParamValues().get(param));

            customElement.appendChild(paramElement);
        }

        return customElement;
    }

    public Element createNode(AddForeignKeyConstraintChange change) {
        Element node = currentChangeLogFileDOM.createElement(change.getChangeMetaData().getName());

        if (change.getBaseTableSchemaName() != null) {
            node.setAttribute("baseTableSchemaName", change.getBaseTableSchemaName());
        }

        node.setAttribute("baseTableName", change.getBaseTableName());
        node.setAttribute("baseColumnNames", change.getBaseColumnNames());
        node.setAttribute("constraintName", change.getConstraintName());

        if (change.getReferencedTableSchemaName() != null) {
            node.setAttribute("referencedTableSchemaName", change.getReferencedTableSchemaName());
        }
        node.setAttribute("referencedTableName", change.getReferencedTableName());
        node.setAttribute("referencedColumnNames", change.getReferencedColumnNames());

        if (change.getDeferrable() != null) {
            node.setAttribute("deferrable", change.getDeferrable().toString());
        }

        if (change.getInitiallyDeferred() != null) {
            node.setAttribute("initiallyDeferred", change.getInitiallyDeferred().toString());
        }

//        if (getDeleteCascade() != null) {
//            node.setAttribute("deleteCascade", getDeleteCascade().toString());
//        }

        if (change.getUpdateRule() != null) {
            switch (change.getUpdateRule()) {
                case DatabaseMetaData.importedKeyCascade:
                    node.setAttribute("onUpdate", "CASCADE");
                    break;
                case DatabaseMetaData.importedKeySetNull:
                    node.setAttribute("onUpdate", "SET NULL");
                    break;
                case DatabaseMetaData.importedKeySetDefault:
                    node.setAttribute("onUpdate", "SET DEFAULT");
                    break;
                case DatabaseMetaData.importedKeyRestrict:
                    node.setAttribute("onUpdate", "RESTRICT");
                    break;
                default:
                    //don't set anything
//                    node.setAttribute("onUpdate", "NO ACTION");
                    break;
            }
        }
        if (change.getDeleteRule() != null) {
            switch (change.getDeleteRule()) {
                case DatabaseMetaData.importedKeyCascade:
                    node.setAttribute("onDelete", "CASCADE");
                    break;
                case DatabaseMetaData.importedKeySetNull:
                    node.setAttribute("onDelete", "SET NULL");
                    break;
                case DatabaseMetaData.importedKeySetDefault:
                    node.setAttribute("onDelete", "SET DEFAULT");
                    break;
                case DatabaseMetaData.importedKeyRestrict:
                    node.setAttribute("onDelete", "RESTRICT");
                    break;
                default:
                    //don't set anything
//                    node.setAttribute("onDelete", "NO ACTION");
                    break;
            }
        }
        return node;
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
